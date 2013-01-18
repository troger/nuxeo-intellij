# Nuxeo IntelliJ

Provides Nuxeo support for IntelliJ IDEA.

Note that this plugin is not officially supported by Nuxeo.

## Features

Main features are:

* Registration of Nuxeo SDKs;
* Selection of the default SDK to use;
* Auto detection of Nuxeo modules;
* Hot reload of Nuxeo module marked as hot reloadable.

## Developers

You can open the project by opening the `nuxeo-intellij.iml` file.

You need to update the IntelliJ IDEA Plugin SDK for the Nuxeo plugin to point to your SDK.

The Nuxeo plugin depends on the `properties` plugin, so you need to add it to your Plugin SDK. The jar can be found in you IntelliJ IDEA installation: `IDEA_HOME/plugins/properties/lib/properties.jar`


## License

Licensed under the MIT License, see the `LICENSE` file.
