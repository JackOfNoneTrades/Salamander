/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.github.alexthe666.citadel.client.model.container.TabulaCubeContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaCubeGroupContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaModelContainer;
import com.google.gson.Gson;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Loads legacy Tabula {@code .tbl} archives and extracted {@code model.json} streams. */
@Deprecated
@SideOnly(Side.CLIENT)
public enum TabulaModelHandler {

    INSTANCE;

    private final Gson gson = new Gson();

    public TabulaModelContainer loadTabulaModel(String path) throws IOException {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        if (!resourcePath.endsWith(".tbl")) resourcePath += ".tbl";

        InputStream stream = TabulaModelHandler.class.getResourceAsStream(resourcePath);
        if (stream == null) throw new IOException("Tabula model not found: " + resourcePath);

        try (InputStream modelJson = getModelJsonStream(resourcePath, stream)) {
            return loadTabulaModel(modelJson);
        }
    }

    public TabulaModelContainer loadTabulaModel(InputStream stream) {
        if (stream == null) throw new IllegalArgumentException("Tabula model stream cannot be null");
        return this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), TabulaModelContainer.class);
    }

    public TabulaCubeContainer getCubeByName(String name, TabulaModelContainer model) {
        for (TabulaCubeContainer cube : getAllCubes(model)) {
            if (cube.getName()
                .equals(name)) return cube;
        }

        return null;
    }

    public TabulaCubeContainer getCubeByIdentifier(String identifier, TabulaModelContainer model) {
        for (TabulaCubeContainer cube : getAllCubes(model)) {
            if (cube.getIdentifier()
                .equals(identifier)) return cube;
        }

        return null;
    }

    public List<TabulaCubeContainer> getAllCubes(TabulaModelContainer model) {
        List<TabulaCubeContainer> cubes = new ArrayList<>();

        for (TabulaCubeGroupContainer group : model.getCubeGroups()) cubes.addAll(traverse(group));
        for (TabulaCubeContainer cube : model.getCubes()) cubes.addAll(traverse(cube));

        return cubes;
    }

    private List<TabulaCubeContainer> traverse(TabulaCubeGroupContainer group) {
        List<TabulaCubeContainer> cubes = new ArrayList<>();

        for (TabulaCubeContainer child : group.getCubes()) cubes.addAll(traverse(child));
        for (TabulaCubeGroupContainer child : group.getCubeGroups()) cubes.addAll(traverse(child));

        return cubes;
    }

    private List<TabulaCubeContainer> traverse(TabulaCubeContainer cube) {
        List<TabulaCubeContainer> cubes = new ArrayList<>();
        cubes.add(cube);

        for (TabulaCubeContainer child : cube.getChildren()) cubes.addAll(traverse(child));

        return cubes;
    }

    private static InputStream getModelJsonStream(String name, InputStream file) throws IOException {
        ZipInputStream archive = new ZipInputStream(file);
        ZipEntry entry;

        while ((entry = archive.getNextEntry()) != null) {
            if ("model.json".equals(entry.getName())) return archive;
        }

        archive.close();
        throw new IOException("No model.json present in " + name);
    }
}
