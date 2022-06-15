package com.vaadin.componentfactory.enhancedgrid;

/*
 * #%L
 * enhanced-grid-flow
 * %%
 * Copyright (C) 2020 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ApplyFilterListener;
import com.vaadin.flow.component.grid.CancelEditConfirmDialog;
import com.vaadin.flow.component.grid.Filter;
import com.vaadin.flow.component.grid.FilterAppliedEvent;
import com.vaadin.flow.component.grid.FilterClickedEvent;
import com.vaadin.flow.component.grid.FilterField;
import com.vaadin.flow.component.grid.FilterFieldDto;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.component.grid.GridArrayUpdater.UpdateQueueData;
import com.vaadin.flow.component.grid.filtering.BackendFilterFieldDto;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveEvent.ContinueNavigationAction;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * Add a selectionPredicate to forbid the grid selection for specific rows
 * Add a editablePredicate to forbid the edition for specific rows
 *
 * @param <T>
 */
@CssImport(value = "./styles/enhanced-grid-selection-disabled.css", themeFor = "vaadin-grid")
public class EnhancedGrid<T> extends Grid<T> implements BeforeLeaveObserver, ApplyFilterListener {

	protected static final Logger log = LoggerFactory.getLogger(EnhancedGrid.class.getName());

	private static final String CANCEL_EDIT_MSG_KEY = "cancel-edit-dialog.text";
	    
    private static final String CANCEL_EDIT_CONFIRM_BTN_KEY = "cancel-edit-dialog.confirm-btn";
    
    private static final String CANCEL_EDIT_CANCEL_BTN_KEY = "cancel-edit-dialog.cancel-btn";
    
    private SerializablePredicate<T> selectionPredicate = item -> true;
    
    private DataGenerator<T> generateSelectionGenerator;
    
    private SerializablePredicate<T> editablePredicate = item -> true;
        
    private boolean showCancelEditDialog = true;

	private Specification<T> filterSpecification = null;

	private boolean filterBackend = true;
    	
    SerializableFunction<T, String> selectionDisabled = new SerializableFunction<T, String>() {

    	@Override
		public String apply(T item) {
			if(!selectionPredicate.test(item)) {
    			return "selection-disabled";
    		}
			return "";
		}
    	
	};
	
	SerializableFunction<T, String> defaultClassNameGenerator = new SerializableFunction<T, String>() {

    	@Override
		public String apply(T item) {
			return "";
		}
    	
	};
    
    /**
     * @see Grid#Grid()
     */
    public EnhancedGrid() {
        super();
    }

    /**
     *
     * @see Grid#Grid(int)
     *
     * @param pageSize - the page size. Must be greater than zero.
     */
    public EnhancedGrid(int pageSize) {
        super(pageSize);
    }

    /**
     *
     * @see Grid#Grid(Class, boolean)
     *
     * @param beanType - the bean type to use, not null
     * @param autoCreateColumns – when true, columns are created automatically for the properties of the beanType
     */
    public EnhancedGrid(Class<T> beanType, boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
    }

    /**
     *
     * @see Grid#Grid(Class)
     *
     * @param beanType - the bean type to use, not null
     */
    public EnhancedGrid(Class<T> beanType) {
        super(beanType);
    }
    
    /**
     * 
     * @see Grid#Grid(Class, SerializableBiFunction, DataCommunicatorBuilder)
     * 
     * @param <U>
     * @param <B>
     * @param beanType
     * @param updateQueueBuilder
     * @param dataCommunicatorBuilder
     */
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> EnhancedGrid(
            Class<T> beanType,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuilder,
            B dataCommunicatorBuilder){
    	super(beanType, updateQueueBuilder, dataCommunicatorBuilder);
    }
    
    /**
     * 
     * @see Grid#Grid(int, SerializableBiFunction, DataCommunicatorBuilder)
     * 
     * @param <U>
     * @param <B>
     * @param pageSize
     * @param updateQueueBuilder
     * @param dataCommunicatorBuilder
     */
    protected <U extends GridArrayUpdater, B extends DataCommunicatorBuilder<T, U>> EnhancedGrid(
            int pageSize,
            SerializableBiFunction<UpdateQueueData, Integer, UpdateQueue> updateQueueBuilder,
            B dataCommunicatorBuilder) {
    	super(pageSize, updateQueueBuilder, dataCommunicatorBuilder);
    }
        
    /**
     * Define if an item can be selected.
     * 
     * @return
     */
    public SerializablePredicate<T> getSelectionPredicate() {
        return selectionPredicate;
    }

    /**
     * Disable selection/deselection of the item that doesn't match the selectionPredicate
     *
     * @param selectionPredicate selectionPredicate
     */
    public void setSelectionPredicate(SerializablePredicate<T> selectionPredicate) {
        this.selectionPredicate = selectionPredicate;
        if (generateSelectionGenerator != null) {
          generateSelectionGenerator.destroyAllData();
        }
        generateSelectionGenerator = this::generateSelectionAccess;
        addDataGenerator(generateSelectionGenerator);
                              
        super.setClassNameGenerator(item -> selectionDisabled.apply(item).concat(" ").concat(defaultClassNameGenerator.apply(item))); 
    }
          
	@Override
	public void setClassNameGenerator(SerializableFunction<T, String> classNameGenerator) {
		defaultClassNameGenerator = classNameGenerator;	
		super.setClassNameGenerator(item -> selectionDisabled.apply(item).concat(" ").concat(defaultClassNameGenerator.apply(item)));
	}
        
    /**
     * Add a selectionDisabled value on the client side
     *
     * @param item item
     * @param jsonObject jsonObject
     */
    private void generateSelectionAccess(T item, JsonObject jsonObject) {
        if (!selectionPredicate.test(item)) {
            jsonObject.put("selectionDisabled", true);
        }
    }
    
	/**
	 * Disabling this method to disable custom selection mode from enhanced grid (which has a client JS bug)
	 * but use standard vaadin selection mode instead
	 */
    // @Override
    // public GridSelectionModel<T> setSelectionMode(SelectionMode selectionMode) {
    //     if (selectionMode == SelectionMode.MULTI) {

    //         Objects.requireNonNull(selectionMode, "Selection mode cannot be null.");
    //         GridSelectionModel<T> model = new CustomAbstractGridMultiSelectionModel<T>(this) {

    //             @Override
    //             protected void fireSelectionEvent(SelectionEvent<Grid<T>, T> event) {
    //                 ComponentUtil.fireEvent(getGrid(), (ComponentEvent<Grid<?>>) event);
    //             }
    //         };
    //         setSelectionModel(model, selectionMode);
    //         return model;
    //     } else  if (selectionMode == SelectionMode.SINGLE) {
    //         GridSelectionModel<T> model = new CustomAbstractGridSingleSelectionModel<T>(this) {

    //             @Override
    //             protected void fireSelectionEvent(SelectionEvent<Grid<T>, T> event) {
    //                 ComponentUtil.fireEvent(getGrid(), (ComponentEvent<Grid<?>>) event);
    //             }

    //             @Override
    //             public void setDeselectAllowed(boolean deselectAllowed) {
    //                 super.setDeselectAllowed(deselectAllowed);
    //                 getGrid().getElement().executeJs(
    //                     "this.$connector.deselectAllowed = $0",
    //                     deselectAllowed);
    //             }
    //         };
    //         setSelectionModel(model, selectionMode);
    //         return model;
    //     } else {
    //         return super.setSelectionMode(selectionMode);
    //     }
    // }
    
   
    /**
     * Define if an item can be edited.
     * 
     * @param editablePredicate
     */
    public void setEditablePredicate(SerializablePredicate<T> editablePredicate) {
        this.editablePredicate = editablePredicate;
    }
    
    /**
     * Return whether an item is editable or not.
     * 
     * @param item
     * @return
     */
    public boolean isEditable(T item) {
    	return this.editablePredicate.test(item);
    }
        
    /**
     * Edit the selected item.
     * 
     * @param item
     */
    public void editItem(T item) {
        if(!isEditable(item)) {
    		return;
    	}
        
        T onEditItem = this.getEditor().getItem();    	    	
    	if(onEditItem != null && item.equals(onEditItem)) {
    		return;
    	}
    	
		if(onEditItem != null && !item.equals(onEditItem) && allowCancelEditDialogDisplay()) {
			cancelEditItem(item, null, null);
		} else {
			this.getEditor().editItem(item);
		}        
    }
    
    /**
     * Cancel the current item edition.
     * 
     */
    public void cancelEdit() {
    	if(this.getEditor().getItem() != null) {
	    	if(allowCancelEditDialogDisplay()) {
				cancelEditItem(null, null, null);
			} else {
				this.getEditor().cancel();
			}
    	}
    }
    
    /**
     * Cancel the current item edition with an specific callback for cancel action. 
     * 
     * @param onCancelCallback
     */
    protected void cancelEditWithCancelCallback(SerializableRunnable onCancelCallback) {
    	if(this.getEditor().getItem() != null) {
	    	if(allowCancelEditDialogDisplay()) {
	    	  	cancelEditItem(null, null, onCancelCallback);
			} else {
				this.getEditor().cancel();
			}
    	}      
    }
    
    protected void cancelEditItem(T newEditItem, ContinueNavigationAction action, SerializableRunnable onCancelCallback) {
    	String text = getTranslation(CANCEL_EDIT_MSG_KEY);
    	String confirmText = getTranslation(CANCEL_EDIT_CONFIRM_BTN_KEY); 
    	String cancelText = getTranslation(CANCEL_EDIT_CANCEL_BTN_KEY);
       	SerializableRunnable onConfirmCallback = action != null ? () -> this.onConfirmEditItem(newEditItem, action) : () -> this.onConfirmEditItem(newEditItem);
       	new CancelEditConfirmDialog(text, confirmText, cancelText, onConfirmCallback, onCancelCallback).open();
    }

    private void onConfirmEditItem(T newEditItem) {
    	this.getEditor().cancel();
		if(newEditItem != null) {
			this.getEditor().editItem(newEditItem);
		}
    }
    
    private void onConfirmEditItem(T newEditItem, ContinueNavigationAction action) {
    	this.onConfirmEditItem(null);
    	action.proceed(); 
    }
       
	/**
	 * Set showCancelEditDialog value to know if {@link CancelEditConfirmDialog} should be displayed.
	 * 
	 * @param showCancelEditDialog
	 */
	public void setShowCancelEditDialog(boolean showCancelEditDialog) {
		this.showCancelEditDialog = showCancelEditDialog;
	}
    
	/**
	 * {@link CancelEditConfirmDialog} will be displayed if showCancelEditDialog 
	 * is true and editor is in buffered mode.
	 * 
	 * @return
	 */
	protected boolean allowCancelEditDialogDisplay() {
		return showCancelEditDialog && this.getEditor().isBuffered();
	}	
    
	@Override
	public void beforeLeave(BeforeLeaveEvent event) {
		T onEditItem = this.getEditor().getItem();
		if(onEditItem != null && allowCancelEditDialogDisplay()) {
			ContinueNavigationAction action = event.postpone();
			cancelEditItem(null, action, null);
		}		
	}	
	
	/**
	 * @see Grid#getDefaultColumnFactory()
	 * 
	 */
	@Override
	protected BiFunction<Renderer<T>, String, Column<T>> getDefaultColumnFactory() {
		return (renderer, columnId) -> new EnhancedColumn<>(this, columnId, renderer);
	}
	
	/**
	 * @see Grid#addColumn(ValueProvider)
	 * 
	 */
	@Override
	public EnhancedColumn<T> addColumn(ValueProvider<T, ?> valueProvider) {
        return (EnhancedColumn<T>) super.addColumn(valueProvider);
    }
	
	@SuppressWarnings("unchecked")
	@Override
	protected <C extends Column<T>> C addColumn(ValueProvider<T, ?> valueProvider,
		BiFunction<Renderer<T>, String, C> columnFactory) {
		EnhancedColumn<T> column = (EnhancedColumn<T>) super.addColumn(valueProvider, columnFactory);
		column.setValueProvider(valueProvider);
		return (C) column;
	}
	
	/**
	 * @see Grid#addColumn(ValueProvider, String...)
	 * 
	 */
	@Override
	public <V extends Comparable<? super V>> EnhancedColumn<T> addColumn(ValueProvider<T, V> valueProvider,
			String... sortingProperties) {
		return (EnhancedColumn<T>)super.addColumn(valueProvider, sortingProperties);
	}

	/**
	 * @see Grid#addColumn(Renderer)
	 *  
	 */
	@Override
	public EnhancedColumn<T> addColumn(Renderer<T> renderer) {
		return (EnhancedColumn<T>) super.addColumn(renderer);
	}
	
	/**
	 * @see Grid#addColumn(Renderer, String...)
	 * 
	 */
	@Override
	public EnhancedColumn<T> addColumn(Renderer<T> renderer, String... sortingProperties) {
		return (EnhancedColumn<T>) super.addColumn(renderer, sortingProperties);
	}
		
	/**
	 * @see Grid#addComponentColumn(ValueProvider)
	 * 
	 */
	@Override
	public <V extends Component> EnhancedColumn<T> addComponentColumn(ValueProvider<T, V> componentProvider) {
		return (EnhancedColumn<T>) super.addComponentColumn(componentProvider);
	}
	
	/**
	 * @see Grid#getColumnByKey(String)
	 * 
	 */
	@Override
	public EnhancedColumn<T> getColumnByKey(String columnKey) {
		return (EnhancedColumn<T>) super.getColumnByKey(columnKey);
	}

	protected void setColumnKey(String key, EnhancedColumn<T> column) {
		super.setColumnKey(key, column);
	}
	
	@Override
	public void onApplyFilter(Object filter) {
		applyFilter();
	}

	public void applyFilter() {
		if (filterBackend)
			applyBackendFilter();
		else
			applyFrontendFilter();
	}
	
	/**
	 * Apply the filters selected for each column in {@link FilterField}
	 * 
	 */
	public void applyFrontendFilter() {
		List<Predicate<T>> predicates = new ArrayList<>();
		for(Column<T> column : getColumns()) {
			EnhancedColumn<T> enhancedColumn = (EnhancedColumn<T>)column;
			if(enhancedColumn.getFilter() != null) {
				ValueProvider<T, ?> columnValueProvider = enhancedColumn.getValueProvider();
				Predicate<Object> filterPredicate = enhancedColumn.getFilter().getValue().getFilterPredicate();
				predicates.add(p -> filterPredicate.test(columnValueProvider.apply(p))); 
				enhancedColumn.updateFilterButtonStyle();
			}
		}
		
		SerializablePredicate<T> finalPredicate = t -> {
			for(Predicate<T> predicate : predicates) {
				if(!predicate.test(t)) {
					return false;
				}					
			}
			return true;
		};
		
		applyFilterPredicate(finalPredicate);		
	}	

	/**
	 * Apply filter predicate depending on the data provider
	 *
	 * @param finalPredicate
	 */
	protected void applyFilterPredicate(SerializablePredicate<T> finalPredicate) {
		DataProvider<T, ?> dataProvider = getDataProvider();
		if(dataProvider instanceof ListDataProvider<?>) {
			((ListDataProvider<T>)dataProvider).setFilter(finalPredicate);
		} else if(dataProvider instanceof ConfigurableFilterDataProvider){
			((ConfigurableFilterDataProvider<T, Void, Filter>)dataProvider).setFilter(new Filter<T>(finalPredicate));
		}
	}

	/**
	 * Apply the filters selected for each column in {@link FilterField}
	 *
	 */
	public void applyBackendFilter() {
		Specification<T> finalSpecification = Specification.where(null);
		Boolean filterActive = false;
		Boolean fireEvent = false;

		for (Column<T> column : getColumns()) {
			EnhancedColumn<T> enhancedColumn = (EnhancedColumn<T>) column;
			if (enhancedColumn.getFilter() != null) {
				FilterFieldDto filterFieldDto = enhancedColumn.getFilter().getValue();
				if (filterFieldDto instanceof BackendFilterFieldDto) {
					BackendFilterFieldDto backendFilterFieldDto = (BackendFilterFieldDto) filterFieldDto;
					Specification spec = backendFilterFieldDto.getFilterSpecification();
					enhancedColumn.updateFilterButtonStyle();

					// skip if no filter set for column
					if (spec == null)
						continue;

					finalSpecification = finalSpecification.and(spec);
					filterActive = true;
				}
			}
		}

		applyFilterSpecification(finalSpecification);

		// filter has been deactivated
		if (Boolean.FALSE.equals(filterActive) && filterSpecification != null)
			fireEvent = true;

		// set filter spec to null of no more filter is active
		filterSpecification = Boolean.TRUE.equals(filterActive) ? finalSpecification : null;

		// notify all listeners if filter was resetted (is new) of not empty
		if (filterSpecification != null)
			fireEvent = true;

		// fire an event?
		if (Boolean.TRUE.equals(fireEvent))
			fireEvent(new FilterAppliedEvent<>(this, false, filterSpecification));
	}

	/**
	 * Apply filter specifcation depending on the data provider (if any)
	 *
	 * @param finalSpecification the specification to be applied
	 */
	protected void applyFilterSpecification(Specification<T> finalSpecification) {
		DataProvider<T, ?> dataProvider = getDataProvider();
		if (dataProvider instanceof ConfigurableFilterDataProvider<?, ?, ?>) {
			try {
				ConfigurableFilterDataProvider<T, Void, Specification<T>> filterDataProvider = (ConfigurableFilterDataProvider<T, Void, Specification<T>>) dataProvider;
				filterDataProvider.setFilter(finalSpecification);
			} catch (ClassCastException e) {
				log.warn("COuld not cast data provider as expected.");
			}
		}
	}

	/**
	 * Clear all selected filters and updates the displayed data.
	 *
	 * @return Boolean true is a filter is activated
	 */
	public Boolean isFiltered() {
		for (Column<T> column : getColumns()) {
			EnhancedColumn<T> enhancedColumn = (EnhancedColumn<T>) column;
			if (enhancedColumn.getFilter() != null
					&& !enhancedColumn.getFilter().isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Clear all selected filters and updates the displayed data.
	 * 
	 */
	public void clearAllFilters() {
		for(Column<T> column : getColumns()) {
			EnhancedColumn<T> enhancedColumn = (EnhancedColumn<T>)column;
			if(enhancedColumn.getFilter() != null) {			
				enhancedColumn.clearFilter();
			}
		}
		applyFilter();
	}	
	
	/**
    * Add listener on filter-clicked event.
    * 
    * @param listener
    * @return registration which can remove the listener.
    */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Registration addFilterClickedEventListener(ComponentEventListener<FilterClickedEvent<T>> listener) {
       return addListener(FilterClickedEvent.class, (ComponentEventListener) listener);
   }

   /**
	* Add listener on filter-apply event.
	*
	* @param listener that is notified when filter is applied
	* @return registration which can remove the listener.
	*/
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Registration addFilterAppliedEventListener(ComponentEventListener<FilterAppliedEvent<T>> listener) {
	   return addListener(FilterAppliedEvent.class, (ComponentEventListener) listener);
   }

   public void setBackendFiltering(boolean filterBackend) {
	   this.filterBackend = filterBackend;
   }

   public boolean getBackendFiltering() {
	   return this.filterBackend;
   }

   public Specification<T> getFilterSpecification() {
	   return this.filterSpecification;
   }
	   
}

