package edu.uci.ics.jung.layout3d.util;

/**
 * interface for support for LayoutEvents
 *
 * @param <N>
 */
public interface LayoutChangeListener<N> {

  void layoutChanged(LayoutEvent<N> evt);

  //  void layoutChanged(LayoutNetworkEvent<N> evt);
}
