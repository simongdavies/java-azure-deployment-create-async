package com.fabrikam;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.*;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.rest.LogLevel;
import rx.functions.Func1;
import java.io.BufferedReader;
import java.io.File;
import rx.Observable;
import java.util.Scanner;
import javax.json.Json;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.lang3.time.StopWatch;

public class App 
{
    public static void main( String[] args )
    {
        try {    
            
            // Get the Azure settings for auth from file (azureauth.properties)
            
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            
            // Generate a new RG and deployment name for each run 
            
            final long suffix=System.nanoTime();
            final String resourceGroupName=String.format("javadeploymentTestRG-%d",suffix);
            final String deploymentName=String.format("javaTest-%d",suffix);

            final Region region=Region.UK_WEST;
            
            // Use the Simple VM Template with SSH Key auth from GH quickstarts
            
            final String templateUri="https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vm-sshkey/azuredeploy.json";
            final String templateContentVersion="1.0.0.0";       

            // Load the default SSH Public key and use it for the VM.

            final String sshKey=GetSSHPublicKey();
            
            // Template only needs an SSH Key parameter
            
            final String parameters=Json.createObjectBuilder()
                    .add("sshKeyData",Json.createObjectBuilder()
                        .add("value",sshKey))
                    .build()
                    .toString();

            // Log in to Azure

            final Azure azure = Azure.configure()
                .withLogLevel(LogLevel.BASIC)
                .authenticate(credFile)
                .withDefaultSubscription();
           
            StopWatch stopwatch = new StopWatch();

            // Create an RG for the deployment asynchronously, once it is complete deploy the template asynchronously
            // Once the template is deployed return the VM 

            System.out.println(String.format("Creating resource group %s",resourceGroupName));

            VirtualMachine createdVM= azure.resourceGroups()
                .define(resourceGroupName)
                .withRegion(region)
                .createAsync()
                .flatMap(
                    new Func1<Indexable, Observable<Indexable>>() {
                    @Override
                    public  Observable<Indexable> call(Indexable createdResourceGroup) {            
                        try {
                           
                            System.out.println("Creating deployment ...");
                            stopwatch.start();

                            Observable<Indexable> result= azure.deployments()
                            .define(deploymentName)
                            .withExistingResourceGroup(resourceGroupName)
                            .withTemplateLink(templateUri, templateContentVersion)
                            .withParameters(parameters)
                            .withMode(DeploymentMode.COMPLETE)
                            .createAsync();
                            return result;
                        }
                        catch (Throwable t) {
                            return Observable.error(t);
                        }
                    }
                })
                .map(
                    new Func1<Indexable , VirtualMachine>() {
                    @Override
                    public  VirtualMachine call(Indexable resource) { 
                        stopwatch.stop();         
                        // The RG should have one VM, return it 
                        PagedList<VirtualMachine> vms= azure.virtualMachines().listByResourceGroup(resourceGroupName);
                        return vms.get(0);
                    }
                })
                .toBlocking()
                .first();

            // Get the status of the VM

            createdVM.instanceView().statuses().forEach(status->System.out.println("VM Status: " +status.displayStatus()));
            System.out.println("Deployment Complete in " + (stopwatch.getTime() / 1000) + " seconds)");

            // Done, now pause before deleting the Resource Group

            System.out.println(String.format("Press Enter to Delete resource group %s",resourceGroupName));
            Scanner input = new Scanner(System.in);
            input.nextLine();

            azure.resourceGroups().deleteByNameAsync(resourceGroupName).await();
            System.out.println(String.format("Successfully Deleted resource group %s",resourceGroupName));

            input.close();
        
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
       
    }
    // load the default ssh public key
    public static String GetSSHPublicKey() throws Exception
    {
        String filename=String.format("%s/.ssh/id_rsa.pub",System.getProperty("user.home"));
        try (BufferedReader reader = new BufferedReader(new FileReader(filename)))
        {
            StringBuilder sb= new StringBuilder();
            String s;
            while ((s =reader.readLine())!=null)
            {
                sb.append(s);
            }
            return sb.toString();
        }
        catch (IOException ex) 
        {
             throw new Exception(ex);
        }

    }  
}