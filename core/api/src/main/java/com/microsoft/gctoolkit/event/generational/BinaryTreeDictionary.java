// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

public class BinaryTreeDictionary {

    private long totalFreeSpace;
    private long maxChunkSize;
    private int numberOfBlocks;
    private long averageBlockSize;
    private int treeHeight;

    public BinaryTreeDictionary(long totalFreeSpace, long maxChunkSize, int numberOfBlocks, long averageBlockSize, int treeHeight) {
        this.totalFreeSpace = totalFreeSpace;
        this.maxChunkSize = maxChunkSize;
        this.numberOfBlocks = numberOfBlocks;
        this.averageBlockSize = averageBlockSize;
        this.treeHeight = treeHeight;
    }

    public long getTotalFreeSpace() {
        return totalFreeSpace;
    }

    public long getMaxChunkSize() {
        return maxChunkSize;
    }

    public int getNumberOfBlocks() {
        return this.numberOfBlocks;
    }

    public long getAverageBlockSize() {
        return averageBlockSize;
    }

    public int getTreeHeight() {
        return treeHeight;
    }
}
