package com.thefryguy.frytools.client;

import com.thefryguy.frytools.client.ui.FryToolsWindow;
import net.fabricmc.api.ClientModInitializer;

public class FrytoolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This runs when the CLIENT starts
        FryToolsWindow.start(); // Launch our window immediately
    }
}