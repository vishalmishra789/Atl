package com.kevin.tiertagger.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi; // Make sure this import is here

// YOU MUST ADD: implements ModMenuApi
public class UkulibIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // This links your config screen to the ModMenu button
        return parent -> new TTConfigScreen(parent); 
    }
}
