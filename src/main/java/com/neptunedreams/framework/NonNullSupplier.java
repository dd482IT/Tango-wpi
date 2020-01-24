package com.neptunedreams.framework;

import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface NonNullSupplier<R> {
  @NonNull R get();
}
