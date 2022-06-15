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

import java.time.LocalDate;
import java.util.function.Function;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;

public class LocalDateFilterField<R> extends AbstractCompositeField<Div, LocalDateFilterField<R>, LocalDateFieldFilterDto<R>> {


	private DatePicker filterStart;

	private DatePicker filterEnd;

	private Binder<LocalDateFieldFilterDto> binder;

	public LocalDateFilterField() {
		super(new LocalDateFieldFilterDto<>());
		init();
	}

	public LocalDateFilterField(LocalDateFieldFilterDto<R> dto) {
		super(dto);
		init();
	}

	public LocalDateFilterField(SingularAttribute<R, LocalDate> specificationField) {
		super(new LocalDateFieldFilterDto<>(specificationField));
		init();
	}

	public LocalDateFilterField(SingularAttribute<R, LocalDate> specificationField, LocalDateFieldFilterDto<R> dto) {
		super(dto.specificationField(specificationField));
		init();
	}

	public LocalDateFilterField(Function<Root<R>, Expression<LocalDate>> metaclassFunction) {
		super(new LocalDateFieldFilterDto<>(metaclassFunction));
		init();
	}

	private void init() {
		binder = new Binder<>(LocalDateFieldFilterDto.class);

		filterStart = new DatePicker();
		filterStart.setPlaceholder("von...");
		binder.bind(filterStart, LocalDateFieldFilterDto::getDateFilterStart, LocalDateFieldFilterDto::setDateFilterStart);

		filterEnd = new DatePicker();
		filterEnd.setPlaceholder("...bis");
		binder.bind(filterEnd, LocalDateFieldFilterDto::getDateFilterEnd, LocalDateFieldFilterDto::setDateFilterEnd);

		binder.setBean(getValue());

		getContent().add(new VerticalLayout(filterStart, filterEnd));
	}

	@Override
	public void clear() {
		filterStart.clear();
		filterEnd.clear();
	}

	public boolean isSet() {
		return false && (filterStart != null && !filterStart.isEmpty()) || (filterEnd != null && !filterEnd.isEmpty());
	}

	@Override
	public LocalDateFieldFilterDto<R> getEmptyValue() {
		return new LocalDateFieldFilterDto<>(null, null);
	}

	@Override
	protected void setPresentationValue(LocalDateFieldFilterDto<R> zonedDateTimeFieldFilterDto) {
		if(zonedDateTimeFieldFilterDto == null) {
			this.clear();
		} else {
			filterStart.setValue(zonedDateTimeFieldFilterDto.getDateFilterStart());
			filterEnd.setValue(zonedDateTimeFieldFilterDto.getDateFilterEnd());
		}
	}

}
