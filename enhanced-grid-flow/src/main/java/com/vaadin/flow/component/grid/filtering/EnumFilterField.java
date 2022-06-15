package com.vaadin.flow.component.grid.filtering;

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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.data.binder.Binder;

public class EnumFilterField<R, E extends Enum> extends AbstractCompositeField<Div, EnumFilterField<R, E>, EnumFieldFilterDto<R, E>> {


	private MultiSelectListBox<E> filterValues;

	private Set<E> values;

	private Binder<EnumFieldFilterDto> binder;

	private ItemLabelGenerator<E> itemLabelGenerator;

	public EnumFilterField(SingularAttribute<R, E> specificationField, Set<E> values) {
		super(new EnumFieldFilterDto<>(specificationField));
		this.values = values;

		init();
	}

	public EnumFilterField(SingularAttribute<R, E> specificationField, E[] values) {
		this(specificationField, Arrays.stream(values).collect(Collectors.toSet()));
	}

	private void init() {
		binder = new Binder<>(EnumFieldFilterDto.class);

		filterValues = new MultiSelectListBox<>();
		filterValues.setItems(values);
		filterValues.setWidth("100%");
		binder.bind(filterValues, EnumFieldFilterDto::getFilterValue, EnumFieldFilterDto::setFilterValue);

		binder.setBean(getValue());

		getContent().add(filterValues);
	}

	@Override
	public void clear() {
		filterValues.clear();
	}

	public boolean isSet() {
		return filterValues != null && !filterValues.getSelectedItems().isEmpty();
	}

	@Override
	public EnumFieldFilterDto<R, E> getEmptyValue() {
		return new EnumFieldFilterDto<>(null, null);
	}

	@Override
	protected void setPresentationValue(EnumFieldFilterDto<R, E> enumFieldFilterDto) {
		if(enumFieldFilterDto == null) {
			this.clear();
		} else {
			filterValues.setValue(enumFieldFilterDto.getFilterValue().stream().collect(Collectors.toSet()));
		}
	}

	public MultiSelectListBox<E> getListBox() {
		return this.filterValues;
	}

	public void setListBox(MultiSelectListBox<E> filterValues) {
		this.filterValues = filterValues;
	}


	public EnumFilterField<R, E> itemLabelGenerator(ItemLabelGenerator<E> itemLabelGenerator) {
		setItemLabelGenerator(itemLabelGenerator);
		return this;
	}

	public ItemLabelGenerator<E> getItemLabelGenerator() {
		return this.itemLabelGenerator;
	}

	public void setItemLabelGenerator(ItemLabelGenerator<E> itemLabelGenerator) {
		this.itemLabelGenerator = itemLabelGenerator;
		if (filterValues != null) filterValues.setItemLabelGenerator(itemLabelGenerator);
	}

}
