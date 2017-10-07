# sf-agile-deploy
## Salesforce Agile Deployment Tool

This Salesforce deployment tool is work in progress to simplify Salesforce deployments. This app provide services that enables users of [Salesforce Agile Accelerator](https://appexchange.salesforce.com/listingDetail?listingId=a0N30000000ps3jEAA) to deploy stories with a button-click. It enables developers and admins alike to use source driven develpment with [Github](https://github.com).

The is Java Spring-Boot app to enable metadata deployments. The app is intended for [Heroku](https://www.heroku.com/platform) platform, and can be run on local server or other containers. Example Heroku app can be found [here](https://sf-agile-deploy.herokuapp.com/).

The app provides REST API services to enable deployments of Salesforce metadata using Github repository together with Agile Accelerator and Change Sets. Now admins without git CLI can commit their work by simply make of familair change set and use this tool to pull metadata form DEV source org, commit to Github repository and push the work to target QA or Staging.

### Dependencies

+ [Salesforce Agile Accelerator](https://appexchange.salesforce.com/listingDetail?listingId=a0N30000000ps3jEAA) - Agile Story Manager tool
+ [sf-agile-deploy](https://github.com/iandrosov/sf-agile-deploy) - REST Services on Heroku
+ [ANT Migration tool](https://developer.salesforce.com/page/Force.com_Migration_Tool)
+ [JGit](https://www.eclipse.org/jgit/)


### Java Libraries adn Maven repo

Project depends on set of standard java dependencies that mostly managed by mvn `pom.xml` file. However, the Salesforce migration tool - `ant.salesforce-40.0.0.jar` and Salesforce API wrapper `wcs.ent-40.0.0.jar` were not available in public repositories. I had to include them as local repository in Github. If anyone has better idea how to add these I am open to suggestions. 

### Deploy to Heroku

For simple deployment on Heroku platform use this handy Heroku-button 

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/iandrosov/sf-agile-deploy)

### Environment Set up

This app rely on environment settings to find various user credentials neeeded to perform deployment tasks, such as pull source for DEV ORG, commit to Github etc.
Need to add this to Heroku App Settings.

Heroku App Config Vars

| Variable Name | Value                    |
| ------------- | ------------------------ |
| GITREMOTEURL  | URL to Github Repository |
| GITREPONAME   | Name of repository       |
| GITUSER       | Github User name         |
| GITPASS       | Your Git password        |
| BASEUSEREMAIL | SF User as email address |
| SFPASS        | Password for source DEV org |
| SFSERVERURL   | Login URL https://test.salesforce.com |
| SFPACKAGEHOST | SFDC Url Agile Accelerator installed |
