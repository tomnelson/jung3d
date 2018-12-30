package edu.uci.ics.jung.layout3d.util;

/**
 * interface for support to LayoutChangeListeners
 *
 * @param <N>
 */
public interface LayoutEventSupport<N> {

  void addLayoutChangeListener(LayoutChangeListener<N> listener);

  void removeLayoutChangeListener(LayoutChangeListener<N> listener);
}
