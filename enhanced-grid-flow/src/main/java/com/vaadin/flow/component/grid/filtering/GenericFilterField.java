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

import java.util.function.Function;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class GenericFilterField<R, T> extends AbstractCompositeField<Div, GenericFilterField<R, T>, GenericFieldFilterDto<R, T>> {

	private TextField filter;

	private VerticalLayout optionsLayout;

	private Checkbox wholeField;

	private Checkbox caseSensitive;

	private Checkbox invertResult;

	private Binder<GenericFieldFilterDto> binder;

	public GenericFilterField() {
		super(new GenericFieldFilterDto<>());
		init();
	}

	public GenericFilterField(GenericFieldFilterDto<R, T> dto) {
		super(dto);
		init();
	}

	public GenericFilterField(SingularAttribute<R, T> specificationField) {
		super(new GenericFieldFilterDto<>(specificationField));
		init();
	}

	public GenericFilterField(SingularAttribute<R, T> specificationField, GenericFieldFilterDto<R, T> dto) {
		super(dto.specificationField(specificationField));
		init();
	}

	public GenericFilterField(Function<Root<R>, Expression<T>> metaclassFunction) {
		super(new GenericFieldFilterDto<>(metaclassFunction));
		init();
	}

	private void init() {
		binder = new Binder<>(GenericFieldFilterDto.class);

		optionsLayout = new VerticalLayout();
		optionsLayout.setSpacing(false);
		optionsLayout.setPadding(false);

		wholeField = new Checkbox("Gesamtes Feld");
		binder.bind(wholeField, GenericFieldFilterDto::isWholeField, GenericFieldFilterDto::setWholeField);
		optionsLayout.add(wholeField);

		caseSensitive = new Checkbox("Groß- und Kleinschreibung beachten");
		binder.bind(caseSensitive, GenericFieldFilterDto::isCaseSensitive, GenericFieldFilterDto::setCaseSensitive);
		optionsLayout.add(caseSensitive);

		invertResult = new Checkbox("Filterergebnis umkehren");
		binder.bind(invertResult, GenericFieldFilterDto::isInvertResult, GenericFieldFilterDto::setInvertResult);
		optionsLayout.add(invertResult);

		filter = new TextField();
		filter.addKeyDownListener(Key.ENTER, e -> com.vaadin.flow.component.grid.FilterField.findComponent(this).ifPresent(com.vaadin.flow.component.grid.FilterField::applyFilter));
		filter.setWidth("100%");
		binder.bind(filter, GenericFieldFilterDto::getFilterValue, GenericFieldFilterDto::setFilterValue);

		binder.setBean(getValue());

		getContent().add(filter, optionsLayout);
	}

	@Override
	public void clear() {
		filter.clear();
		wholeField.clear();
		caseSensitive.clear();
		invertResult.clear();
	}

	public boolean isSet() {
		return !filter.isEmpty();
	}

	@Override
	public GenericFieldFilterDto<R, T> getEmptyValue() {
		return new GenericFieldFilterDto<>(false, false, false, StringUtils.EMPTY);
	}

	@Override
	protected void setPresentationValue(GenericFieldFilterDto<R, T> textFieldFilterDto) {
		if(textFieldFilterDto == null) {
			this.clear();
		} else {
			filter.setValue(textFieldFilterDto.getFilterValue());
			wholeField.setValue(textFieldFilterDto.isWholeField());
			caseSensitive.setValue(textFieldFilterDto.isCaseSensitive());
			invertResult.setValue(textFieldFilterDto.isInvertResult());
		}
	}

}
