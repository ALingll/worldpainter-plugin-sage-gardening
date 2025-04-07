package org.cti.wpplugin;

import org.cti.wpplugin.layers.editors.GardeningLayerEditor;
import org.cti.wpplugin.layers.GardeningLayer;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.LayerEditor;
import org.pepsoft.worldpainter.plugins.*;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.cti.wpplugin.Version.VERSION;

/**
 * The main plugin class. This demo combines the various providers in one plugin class. You could of course also
 * separate them out into separate plugins classes for clarity. And of course you can leave out any providers for
 * services your plugin does not provide.
 *
 * <p><strong>Note:</strong> this class is referred to from the {@code org.pepsoft.worldpainter.plugins} file, so when
 * you rename or copy it, be sure to keep that file up-to-date.
 */
@SuppressWarnings("unused") // Instantiated by WorldPainter
public class CustomGardeningPlugin extends AbstractPlugin implements
        // This demo has the plugin class implementing all of these, but they may also be implemented by separate
        // classes, as long as each class implements Plugin and is mentioned in the org.pepsoft.worldpainter.plugins
        // registry file
        //LayerProvider,          // Implement this to provide one or more singular, unconfigurable layers
        CustomLayerProvider,    // Implement this to provide one or more custom layer types, of which users can create more than one with different settings
        LayerEditorProvider    // Implement this to provide a layer settings editor for the custom layer type(s) supported by this plugin. This is mandatory if custom layers are provided
        //OperationProvider,      // Implement this to provide one or more custom operations for the Tools panel
        //CustomObjectProvider    // Implement this to provide a custom object format for the Custom Objects Layer
{
    /**
     * The plugin class must have a default (public, no arguments) constructor.
     */
    public CustomGardeningPlugin() {
        super(NAME, VERSION);
    }


    // CustomLayerProvider

    @Override
    public List<Class<? extends CustomLayer>> getCustomLayers() {
        return CUSTOM_LAYERS;
    }

    // LayerEditorProvider

    @SuppressWarnings("unchecked") // Guaranteed by if statement
    @Override
    public <L extends Layer> LayerEditor<L> createLayerEditor(Platform platform, Class<L> layerType) {
        if (layerType == GardeningLayer.class) {
            return (LayerEditor<L>) new GardeningLayerEditor(platform);
        } else {
            return null;
        }
    }



    /**
     * Short, human-readble name of the plugin.
     */
    static final String NAME = "WorldPainter Plugin Sage-Gardening";

    //private static final List<Layer> LAYERS = singletonList(DemoLayer.INSTANCE);
    private static final List<Class<? extends CustomLayer>> CUSTOM_LAYERS = singletonList(GardeningLayer.class);

}
