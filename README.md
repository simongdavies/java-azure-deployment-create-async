---
services: Compute
platforms: java
author: Simon Davies
---

## Creating deployments asynchronously

  Azure Sample for creating a template deployment asynchronously
  Creates a new RG
  Creates a deployment to create a Linux VM using a template from [Azure quickstarts repo](https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-vm-sshkey/azuredeploy.json)
  Deletes the RG and VM.

## Running this Sample ##

To run this sample:

Make sure you have all the [prerequisites](https://docs.microsoft.com/en-us/java/azure/java-sdk-azure-get-started?view=azure-java-stable#prerequisites)

Set up [authentication](https://docs.microsoft.com/en-us/java/azure/java-sdk-azure-get-started?view=azure-java-stable#set-up-authentication)

    git clone https://github.com/Azure-Samples/compute-java-manage-vm-async.git

    cd compute-java-manage-vm-async

    mvn clean compile exec:java

## More information ##

[http://azure.com/java](http://azure.com/java)

If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)
