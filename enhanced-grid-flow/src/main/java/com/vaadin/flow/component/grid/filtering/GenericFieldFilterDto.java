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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class GenericFieldFilterDto<R, T> implements BackendFilterFieldDto<R, T> {

	private boolean wholeField;

	private boolean caseSensitive;

	private boolean invertResult;

	private String filterValue;

	private SingularAttribute<R, T> specificationField;

	private Function<Root<R>, Expression<T>> metaclassFunction;

	public GenericFieldFilterDto() {
	}

	public GenericFieldFilterDto(SingularAttribute<R, T> specificationField) {
		this.specificationField = specificationField;
	}

	public GenericFieldFilterDto(SingularAttribute<R, T> specificationField, String filterValue) {
		this(specificationField);
		this.filterValue = filterValue;
	}

	public GenericFieldFilterDto(Function<Root<R>, Expression<T>> metaclassFunction) {
		this.metaclassFunction = metaclassFunction;
	}

	public GenericFieldFilterDto(String filterValue) {
		this.filterValue = filterValue;
	}

	public GenericFieldFilterDto(boolean wholeField, boolean caseSensitive,
			boolean invertResult, String filterValue) {
		this.wholeField = wholeField;
		this.caseSensitive = caseSensitive;
		this.invertResult = invertResult;
		this.filterValue = filterValue;
	}

	public GenericFieldFilterDto(SingularAttribute<R, T> specificationField, boolean wholeField,
			boolean caseSensitive,
			boolean invertResult, String filterValue) {
		this(wholeField, caseSensitive, invertResult, filterValue);
		this.specificationField = specificationField;
	}

	public boolean isWholeField() {
		return wholeField;
	}

	public void setWholeField(boolean wholeField) {
		this.wholeField = wholeField;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isInvertResult() {
		return invertResult;
	}

	public void setInvertResult(boolean invertResult) {
		this.invertResult = invertResult;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	public boolean getWholeField() {
		return this.wholeField;
	}

	public boolean getCaseSensitive() {
		return this.caseSensitive;
	}

	public boolean getInvertResult() {
		return this.invertResult;
	}

	public SingularAttribute<R, T> getSpecificationField() {
		return this.specificationField;
	}

	public void setSpecificationField(SingularAttribute<R, T> specificationField) {
		this.specificationField = specificationField;
	}

	public GenericFieldFilterDto<R, T> wholeField(boolean wholeField) {
		setWholeField(wholeField);
		return this;
	}

	public GenericFieldFilterDto<R, T> caseSensitive(boolean caseSensitive) {
		setCaseSensitive(caseSensitive);
		return this;
	}

	public GenericFieldFilterDto<R, T> invertResult(boolean invertResult) {
		setInvertResult(invertResult);
		return this;
	}

	public GenericFieldFilterDto<R, T> filterValue(String filterValue) {
		setFilterValue(filterValue);
		return this;
	}

	public GenericFieldFilterDto<R, T> specificationField(SingularAttribute<R, T> specificationField) {
		setSpecificationField(specificationField);
		return this;
	}

	@Override
	public Predicate<T> getFilterPredicate() {
		Predicate<T> simplePredicate;
		if (StringUtils.isBlank(filterValue)) {
			return s -> true;
		}

		if (wholeField) {
			simplePredicate = caseSensitive ? s -> s.equals(filterValue) : s -> fieldValueToString(s).equalsIgnoreCase(filterValue);
		} else if (caseSensitive) {
			simplePredicate = s -> fieldValueToString(s).contains(filterValue);
		} else {
			simplePredicate = s -> fieldValueToString(s).toUpperCase().contains(filterValue.toUpperCase());
		}

		return invertResult ? simplePredicate.negate() : simplePredicate;
	}

	/**
	 * transforms the fields value that is to be filtered into a string
	 *
	 * @param value field value
	 * @return String version of field value
	 */
	public String fieldValueToString(Object value) {
		return value.toString();
	}

	@Override
	public Specification<R> getFilterSpecification() {
		if (specificationField == null && metaclassFunction == null)
			return null;

		if (StringUtils.isEmpty(filterValue))
			return null;

		Specification<R> spec = Specification.where(null);


		// there is a specification field given
		if(specificationField != null) {
			Function<Root<R>, Expression<T>> expression = r -> r.get(specificationField.getName());
			spec = spec.and(buildSpecificationFromField(expression));
		}

		// only a specifcation given
		if(metaclassFunction != null) {
			spec = spec.and(buildSpecificationFromField(metaclassFunction));
		}

		return invertResult ? Specification.not(spec) : spec;
	}

	private Specification<R> buildSpecificationFromField(Function<Root<R>, Expression<T>> metaclassFunction) {
		Specification<R> spec = Specification.where(null);

		if (wholeField) {
			spec = caseSensitive
					? spec.and((r, cq, cb) -> cb.equal(metaclassFunction.apply(r), filterValue))
					: spec.and((r, cq, cb) -> cb.equal(cb.upper(metaclassFunction.apply(r).as(String.class)),
							filterValue.toUpperCase()));
		} else if (caseSensitive) {
			spec = spec.and((r, cq, cb) -> cb.like(metaclassFunction.apply(r).as(String.class), '%' + filterValue + '%'));
		} else {
			spec = spec.and((r, cq, cb) -> cb.like(cb.upper(metaclassFunction.apply(r).as(String.class)),
					'%' + filterValue.toUpperCase() + '%'));
		}
		return spec;
	}

	@Override
	public boolean isEmpty() {
		return StringUtils.isBlank(filterValue) && !invertResult && !caseSensitive && !wholeField;
	}

	@Override
	public int hashCode() {
		return Objects.hash(caseSensitive, filterValue, invertResult, wholeField);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof GenericFieldFilterDto))
			return false;
		GenericFieldFilterDto<R, T> other = (GenericFieldFilterDto<R, T>) obj;
		return caseSensitive == other.caseSensitive && Objects.equals(filterValue, other.filterValue)
				&& invertResult == other.invertResult && wholeField == other.wholeField;
	}

}
