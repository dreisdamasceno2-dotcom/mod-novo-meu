package melhorias.client;

import melhorias.MelhoriasConfig;
import melhorias.search.FuzzyMatcher;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class FuzzyHooks {
    private static final int LIMIT = 100;

    /** A busca original sempre tem prioridade; a aproximada só entra quando ela não acha nada. */
    private static boolean skip(String q) {
        return !MelhoriasConfig.get.fuzzySearchEnabled || q.isBlank() || q.startsWith("#") || q.startsWith("@");
    }

    public static SearchTree<ItemStack> wrapCreative(SearchTree<ItemStack> original) {
        return query -> {
            List<ItemStack> result = original.search(query);
            if (!result.isEmpty() || skip(query)) return result;
            try {
                Collection<ItemStack> all = CreativeModeTabs.searchTab().getSearchTabDisplayItems();
                return FuzzyMatcher.search(query, all, FuzzyHooks::names, LIMIT, MelhoriasConfig.get.fuzzyMinScore);
            } catch (Throwable t) {
                return result; // nunca quebra a busca original
            }
        };
    }

    /** TODO: a API de receitas foi refeita (RecipeDisplayEntry); por enquanto o livro de receitas usa a busca original. */
    public static SearchTree<RecipeCollection> wrapRecipes(SearchTree<RecipeCollection> original) {
        return original;
    }

    /** Nome no idioma atual + id do registro (em inglês, ex.: diamond_sword) — funciona para itens de outros mods. */
    private static Collection<String> names(ItemStack stack) {
        return List.of(stack.getHoverName().getString(),
                BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().replace('_', ' '));
    }

    private FuzzyHooks() {}
}
