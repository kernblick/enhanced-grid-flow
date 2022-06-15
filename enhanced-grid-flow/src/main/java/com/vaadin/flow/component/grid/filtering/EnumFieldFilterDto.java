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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.jpa.domain.Specification;

public class EnumFieldFilterDto<R, E extends Enum>
		implements BackendFilterFieldDto<R, E> {

	private Set<E> filterValue;

	private SingularAttribute<R, E> specificationField;

	private Function<Root<R>, Expression<E>> metaclassFunction;

	public EnumFieldFilterDto() {
	}

	public EnumFieldFilterDto(SingularAttribute<R, E> specificationField) {
		this.specificationField = specificationField;
	}

	public EnumFieldFilterDto(Function<Root<R>, Expression<E>> metaclassFunction) {
		this.metaclassFunction = metaclassFunction;
	}

	public EnumFieldFilterDto(Set<E> filterValue) {
		this.filterValue = filterValue;
	}

	public EnumFieldFilterDto(SingularAttribute<R, E> specificationField, Set<E> filterValue) {
		this(filterValue);
		this.specificationField = specificationField;
	}

	public SingularAttribute<R, E> getSpecificationField() {
		return this.specificationField;
	}

	public void setSpecificationField(SingularAttribute<R, E> specificationField) {
		this.specificationField = specificationField;
	}

	public EnumFieldFilterDto<R, E> specificationField(SingularAttribute<R, E> specificationField) {
		setSpecificationField(specificationField);
		return this;
	}

	@Override
	public Predicate<E> getFilterPredicate() {
		Predicate<E> simplePredicate = s -> true;

		if (filterValue != null) {
			simplePredicate = simplePredicate.and(s -> filterValue.contains(s));
		}

		return simplePredicate;
	}

	@Override
	public Specification<R> getFilterSpecification() {
		if (specificationField == null && metaclassFunction == null)
			return null;

		if (filterValue == null || filterValue.isEmpty())
			return null;

		if (specificationField == null && metaclassFunction == null)
			return  null;

		Specification<R> spec = Specification.where(null);

		// there is a specification field given
		if (specificationField != null) {
			Function<Root<R>, Expression<E>> expression = r -> r.get(specificationField.getName());
			spec = spec.and(buildSpecificationFromField(expression));
		}

		// only a specifcation given
		if (metaclassFunction != null) {
			spec = spec.and(buildSpecificationFromField(metaclassFunction));
		}

		return spec;
	}

	private Specification<R> buildSpecificationFromField(
			Function<Root<R>, Expression<E>> metaclassFunction) {
		Specification<R> spec = Specification.where(null);

		if (filterValue == null || filterValue.isEmpty())
			return spec;

		return spec.and((r, cq, cb) -> metaclassFunction.apply(r).in(filterValue));
	}

	@Override
	public boolean isEmpty() {
		return filterValue == null || filterValue.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof EnumFieldFilterDto)) {
			return false;
		}
		EnumFieldFilterDto<R, E> enumFieldFilterDto = (EnumFieldFilterDto<R, E>) o;
		return ((filterValue == null || filterValue.isEmpty())
					&& (enumFieldFilterDto.filterValue == null || enumFieldFilterDto.filterValue.isEmpty()))
				|| Objects.equals(filterValue, enumFieldFilterDto.filterValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(filterValue, specificationField, metaclassFunction);
	}

	public Set<E> getFilterValue() {
		return this.filterValue;
	}

	public void setFilterValue(Set<E> filterValue) {
		this.filterValue = filterValue;
	}

}
