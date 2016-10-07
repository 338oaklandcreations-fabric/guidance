Fabric Guidance
==========================

This application is the UI for controlling the Fabric deployment.  It provides monitoring and control access of the deployment.

This application is currently deployed at https://fabric-338-reeds.herokuapp.com

To run it for your own deployment you will need to set the following ENV variables:
* FABRIC_MACHINERY_URL

To set these in Heroku, you can do the following:

```bash
$ heroku config:set FABRIC_MACHINERY_URL=https://***.***.***
Setting config vars and restarting fabric-338... done
FABRIC_MACHINERY_URL: https://***.***.***
```