# Nuxeo IntelliJ

Provides Nuxeo support for IntelliJ IDEA.

Note that this plugin is not officially supported by Nuxeo.

## Compatibility

IntelliJ IDEA 12+, Community and Ultimate editions.

## Features

Main features are:

* Registration of Nuxeo SDKs;
* Selection of the default SDK to use;
* Auto detection of Nuxeo modules;
* Hot reload of Nuxeo module marked as hot reloadable.

## Usage

### Installation

Use IntelliJ IDEA's plugin manager to install the latest version of the plugin.

The plugin can also be find here: http://plugins.intellij.net/plugin/?idea&pluginId=7162

### Download Nuxeo SDK

You can find the latest release of Nuxeo SDK here: [nuxeo-cap-5.6-tomcat-sdk.zip](http://community.nuxeo.com/static/releases/nuxeo-5.6/nuxeo-cap-5.6-tomcat-sdk.zip)

To find the latest snapshot of Nuxeo SDK, you can go [here](https://maven-eu.nuxeo.org/nexus/index.html#nexus-search;gav~org.nuxeo.ecm.distribution~nuxeo-distribution-tomcat~5.7-SNAPSHOT~~nuxeo-cap-sdk).

### Add a Nuxeo SDK

To configure and add a new Nuxeo SDK:

1. Go to the project settings and select Nuxeo / Nuxeo SDKs
2. Click the "+" buton to add a Nuxeo SDK, give it a name and save
3. Reopen the project settings, select Nuxeo and configure the default SDK to use

### Hot reload modules

#### Configure your Nuxeo SDK

To make the hot reload work, you need to deploy the `sdk` Nuxeo template:

* Edit the file `NUXEO_HOME/bin/nuxeo.conf`
* Find the line setting the `nuxeo.templates` and add the `sdk` template. For a default installation you will end up with:

    …
    nuxeo.templates=default,sdk
    …

* Launch Nuxeo

#### Configure the modules

If the Nuxeo facet is not automatically detected for your modules:

1. Go to the Project Structure / Facets
2. Click "+" and select the Nuxeo facet
3. Select your Nuxeo modules
4. Edit the facets related to the modules you want to hot reload and check the `Hot Reloadable` checkbox.

#### Hot reload

When you want to hot reload the configured module on the default Nuxeo SDK, just use the Nuxeo > Nuxeo Hot Reload.

You can also use the `CTRL + ALT + R` shortcut.

You will be notified if the hot reload is successful, or if there is any error.

## Developers

This plugin needs Intellij IDEA 12+.

You can open the project by opening the `nuxeo-intellij.iml` file.

You need to update the IntelliJ IDEA Plugin SDK for the Nuxeo plugin to point to your SDK.

The Nuxeo plugin depends on the `properties` plugin, so you need to add it to your Plugin SDK. The jar can be found in you IntelliJ IDEA installation: `IDEA_HOME/plugins/properties/lib/properties.jar`


## License

Licensed under the MIT License, see the `LICENSE` file.
