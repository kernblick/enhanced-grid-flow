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

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;

public class BooleanFilterField<R> extends AbstractCompositeField<Div, BooleanFilterField<R>, BooleanFieldFilterDto<R>> {


	private Checkbox filterValue;

	private Binder<BooleanFieldFilterDto> binder;

	public BooleanFilterField(SingularAttribute<R, Boolean> specificationField) {
		super(new BooleanFieldFilterDto<>(specificationField));
		init();
	}

	private void init() {
		binder = new Binder<>(BooleanFieldFilterDto.class);

		filterValue = new Checkbox();
		filterValue.setWidth("100%");
		binder.bind(filterValue, BooleanFieldFilterDto::getFilterValue, BooleanFieldFilterDto::setFilterValue);

		binder.setBean(getValue());

		getContent().add(filterValue);
	}

	@Override
	public void clear() {
		filterValue.clear();
	}

	public boolean isSet() {
		return filterValue != null;
	}

	@Override
	public BooleanFieldFilterDto<R> getEmptyValue() {
		return new BooleanFieldFilterDto<>(null, null);
	}

	@Override
	protected void setPresentationValue(BooleanFieldFilterDto<R> enumFieldFilterDto) {
		if(enumFieldFilterDto == null) {
			this.clear();
		} else {
			filterValue.setValue(enumFieldFilterDto.getFilterValue());
		}
	}

}
