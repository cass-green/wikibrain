package org.wikibrain.core.model;

import gnu.trove.map.TIntIntMap;
import gnu.trove.set.TIntSet;
import org.wikibrain.core.lang.Language;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * A compact graph representation of the category structure.
 *
 * The internals of this data structure are public for historical reasons.
 * TODO: It would probably be better to not expose the internal id scheme if it's not a performance hit.
 *
 * @author Matt Lesicko
 * @author Shilad Sen
 */
public class CategoryGraph implements Serializable{
    public Language language;

    // Mapping from local page id to internal dense index.
    public TIntIntMap catIndexes;

    public int[] catIds;    // dense category ids to sparse local page ids
    public double[] catCosts;  // the cost of travelling through each category
    public int[][] catParents;
    public int[][] catPages;
    public int[][] catChildren;
    public String[] cats;
    public double minCost = -1;

    public CategoryGraph(Language language){
        this.language = language;
    }

    public int getCategoryIndex(int catId) {
        return catIndexes.containsKey(catId) ?  catIndexes.get(catId) : -1;
    }

    /**
     * Return the wikipedia page ids for child of the specified category
     * @param wpId
     * @return
     */
    public int[] getChildren(int wpId) {
        int parentIndex = getCategoryIndex(wpId);
        if (parentIndex < 0) {
            return new int[0];
        }
        int [] denseIds = catChildren[parentIndex];
        int childIds[] = new int[denseIds.length];
        for (int i = 0; i < denseIds.length; i++) {
            childIds[i] = catIds[denseIds[i]];
        }
        return childIds;
    }
}