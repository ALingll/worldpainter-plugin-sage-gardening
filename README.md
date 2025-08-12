# WorldPainter Plugin Sage Gardening

This is a plugin for WorldPainter, which provides a new layer called **Gardening Layer**.

**Gardening Layer** provides similar functions with the internal custom plant layer. The difference is that it decouples the "configuration" of plants types from hard coding. This means that by writing JSON in a specific format, the plugin can support any mod or vanilla plant (only the user needs to download or write the corresponding configuration JSON). At the same time, it can also realize many functions that the traditional custom plant layer cannot.

## ChangeLog
- Add a snapshot configuration to 1.21.5 plants.

## TODO
- The current configuration structure writing is too complex and may be simplified in the future.
- Aquatic plants are not currently supported, but may be in the future.
- There is currently no documentation on how to write configuration JSON structures. However, a built-in configuration is provided for [reference](src/main/resources/org/cti/wpplugin/gardening/internal/verdantvibes-2.json), which supports the new vegetation added by the [VerdantVibes](https://github.com/Pandarix/VerdantVibes) mod.

## Configuration

The json file provided by the user should be placed in the `plugin_data\gardening_layer` subdirectory under the worldpainter configuration file directory. For example, on Windows, it would be like `C:\Users\{UserName}\AppData\Roaming\WorldPainter\plugin_data\gardening_layer\{YourJson}.json`

---
© 2025 CTI-ALingll. All rights reserved.
