package edu.uci.ics.jung.layout3d.event;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * For the most general change to a LayoutModel. There is no Event payload, only an indication that
 * there was a change. A visualization would consume the event and re-draw itself. Use-cases for
 * firing this event are when the Graph or LayoutAlgorithm is changed in the LayoutModel
 *
 * @author Tom Nelson
 */
public interface LayoutChange {

  /** indicates support for this type of event dispatch */
  interface Producer {
    edu.uci.ics.jung.layout3d.event.LayoutChange.Support getLayoutChangeSupport();
  }

  /** method signatures to add/remove listeners and fire events */
  interface Support {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutChangeListener(edu.uci.ics.jung.layout3d.event.LayoutChange.Listener l);

    void removeLayoutChangeListener(edu.uci.ics.jung.layout3d.event.LayoutChange.Listener l);

    List<Listener> getLayoutChangeListeners();

    void fireLayoutChanged();
  }

  /** implementation of support. Manages a List of listeners */
  class SupportImpl implements Support {

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<Listener> changeListeners = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      this.fireEvents = fireEvents;
      // fire an event in case anything was missed while inactive
      if (fireEvents) {
        fireLayoutChanged();
      }
    }

    @Override
    public void addLayoutChangeListener(edu.uci.ics.jung.layout3d.event.LayoutChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutChangeListener(edu.uci.ics.jung.layout3d.event.LayoutChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<edu.uci.ics.jung.layout3d.event.LayoutChange.Listener> getLayoutChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutChanged() {
      if (fireEvents) {
        for (int i = changeListeners.size() - 1; i >= 0; i--) {
          changeListeners.get(i).layoutChanged();
        }
      }
    }
  }

  /** implemented by a consumer of this type of event */
  interface Listener {
    void layoutChanged();
  }
}
