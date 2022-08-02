package com.ruyuan.eshop.common.tuple;

import java.io.Serializable;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
public class Pair<L, R> implements Serializable {

    private L left;
    private R right;

    public Pair() {
    }

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L getKey() {
        return this.getLeft();
    }

    public R getValue() {
        return this.getRight();
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public String toString() {
        return "(" + this.getLeft() + ',' + this.getRight() + ')';
    }
}
