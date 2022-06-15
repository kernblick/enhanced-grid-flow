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

import org.springframework.data.jpa.domain.Specification;

public class BooleanFieldFilterDto<R> implements BackendFilterFieldDto<R, Boolean> {

	private Boolean filterValue;

	private SingularAttribute<R, Boolean> specificationField;

	private Function<Root<R>, Expression<Boolean>> metaclassFunction;

	public BooleanFieldFilterDto() {
	}

	public BooleanFieldFilterDto(SingularAttribute<R, Boolean> specificationField) {
		this.specificationField = specificationField;
	}

	public BooleanFieldFilterDto(Function<Root<R>, Expression<Boolean>> metaclassFunction) {
		this.metaclassFunction = metaclassFunction;
	}

	public BooleanFieldFilterDto(Boolean filterValue) {
		this.filterValue = filterValue;
	}

	public BooleanFieldFilterDto(SingularAttribute<R, Boolean> specificationField, Boolean filterValue) {
		this(filterValue);
		this.specificationField = specificationField;
	}

	public SingularAttribute<R, Boolean> getSpecificationField() {
		return this.specificationField;
	}

	public void setSpecificationField(SingularAttribute<R, Boolean> specificationField) {
		this.specificationField = specificationField;
	}

	public BooleanFieldFilterDto<R> specificationField(SingularAttribute<R, Boolean> specificationField) {
		setSpecificationField(specificationField);
		return this;
	}

	@Override
	public Predicate<Boolean> getFilterPredicate() {
		Predicate<Boolean> simplePredicate = s -> true;

		if (filterValue != null) {
			simplePredicate = simplePredicate.and(s -> filterValue.equals(s));
		}

		return simplePredicate;
	}

	@Override
	public Specification<R> getFilterSpecification() {
		if (specificationField == null && metaclassFunction == null)
			return null;

		if (filterValue == null)
			return null;

		Specification<R> spec = Specification.where(null);

		// there is a specification field given
		if (specificationField != null) {
			Function<Root<R>, Expression<Boolean>> expression = r -> r.get(specificationField.getName());
			spec = spec.and(buildSpecificationFromField(expression));
		}

		// only a specifcation given
		if (metaclassFunction != null) {
			spec = spec.and(buildSpecificationFromField(metaclassFunction));
		}

		return spec;
	}

	private Specification<R> buildSpecificationFromField(Function<Root<R>, Expression<Boolean>> metaclassFunction) {
		Specification<R> spec = Specification.where(null);

		if (filterValue == null)
			return null;

		return spec.and((r, cq, cb) -> Boolean.TRUE.equals(filterValue) ? cb.isTrue(metaclassFunction.apply(r)) : cb.isFalse(metaclassFunction.apply(r)));
	}

	@Override
	public boolean isEmpty() {
		return filterValue == null;
	}


	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof BooleanFieldFilterDto)) {
			return false;
		}
		BooleanFieldFilterDto enumFieldFilterDto = (BooleanFieldFilterDto) o;
		return Objects.equals(filterValue, enumFieldFilterDto.filterValue) && Objects.equals(specificationField, enumFieldFilterDto.specificationField) && Objects.equals(metaclassFunction, enumFieldFilterDto.metaclassFunction);
	}

	@Override
	public int hashCode() {
		return Objects.hash(filterValue, specificationField, metaclassFunction);
	}


	public Boolean getFilterValue() {
		return this.filterValue;
	}

	public void setFilterValue(Boolean filterValue) {
		this.filterValue = filterValue;
	}


}
