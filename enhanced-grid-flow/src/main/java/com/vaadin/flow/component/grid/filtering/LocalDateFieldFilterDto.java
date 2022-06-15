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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.jpa.domain.Specification;

public class LocalDateFieldFilterDto<R> implements BackendFilterFieldDto<R, LocalDate> {

	private LocalDate filterStart;
	private LocalDate filterEnd;

	private SingularAttribute<R, LocalDate> specificationField;

	private Function<Root<R>, Expression<LocalDate>> metaclassFunction;

	public LocalDateFieldFilterDto() {
	}

	public LocalDateFieldFilterDto(SingularAttribute<R, LocalDate> specificationField) {
		this.specificationField = specificationField;
	}

	public LocalDateFieldFilterDto(Function<Root<R>, Expression<LocalDate>> metaclassFunction) {
		this.metaclassFunction = metaclassFunction;
	}

	public LocalDateFieldFilterDto(LocalDate filterStart, LocalDate filterEnd) {
		this.filterStart = filterStart;
		this.filterEnd = filterEnd;
	}

	public LocalDateFieldFilterDto(SingularAttribute<R, LocalDate> specificationField,
			LocalDate filterStart, LocalDate filterEnd) {
		this(filterStart, filterEnd);
		this.specificationField = specificationField;
	}

	public SingularAttribute<R, LocalDate> getSpecificationField() {
		return this.specificationField;
	}

	public void setSpecificationField(SingularAttribute<R, LocalDate> specificationField) {
		this.specificationField = specificationField;
	}

	public LocalDateFieldFilterDto<R> specificationField(SingularAttribute<R, LocalDate> specificationField) {
		setSpecificationField(specificationField);
		return this;
	}

	@Override
	public Predicate<LocalDate> getFilterPredicate() {
		Predicate<LocalDate> simplePredicate = s -> true;

		if (filterStart != null) {
			simplePredicate = simplePredicate.and(s -> s.isAfter(filterStart.minusDays(1)));
		}

		if (filterEnd != null) {
			simplePredicate = simplePredicate.and(s -> s.isBefore(filterEnd.plusDays(1)));
		}

		return simplePredicate;
	}

	@Override
	public Specification<R> getFilterSpecification() {
		if (specificationField == null && metaclassFunction == null)
			return null;

		if (filterStart == null && filterEnd == null)
			return null;

		if (specificationField == null && metaclassFunction == null)
			return  null;

		Specification<R> spec = Specification.where(null);

		// there is a specification field given
		if (specificationField != null) {
			Function<Root<R>, Expression<LocalDate>> expression = r -> r.get(specificationField.getName());
			spec = spec.and(buildSpecificationFromField(expression));
		}

		// only a specifcation given
		if (metaclassFunction != null) {
			spec = spec.and(buildSpecificationFromField(metaclassFunction));
		}

		return spec;
	}

	private Specification<R> buildSpecificationFromField(
			Function<Root<R>, Expression<LocalDate>> metaclassFunction) {
		Specification<R> spec = Specification.where(null);

		LocalDate from = filterStart != null ? filterStart
				: LocalDate.ofInstant(Instant.MIN, ZoneId.systemDefault());
		LocalDate to = filterEnd != null ? filterEnd : LocalDate.ofInstant(Instant.MAX, ZoneId.systemDefault());

		return spec.and((r, cq, cb) -> cb.between(metaclassFunction.apply(r), from, to));
	}

	@Override
	public boolean isEmpty() {
		return filterStart == null && filterEnd == null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LocalDateFieldFilterDto)) {
			return false;
		}
		LocalDateFieldFilterDto zonedDateTimeFieldFilterDto = (LocalDateFieldFilterDto) o;
		return Objects.equals(filterStart, zonedDateTimeFieldFilterDto.filterStart)
				&& Objects.equals(filterEnd, zonedDateTimeFieldFilterDto.filterEnd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(filterStart, filterEnd, specificationField, metaclassFunction);
	}

	public void setDateFilterStart(LocalDate filterStart) {
		this.filterStart = filterStart;
	}

	public LocalDate getDateFilterStart() {
		return this.filterStart;
	}

	public void setDateFilterEnd(LocalDate filterEnd) {
		this.filterEnd = filterEnd;
	}

	public LocalDate getDateFilterEnd() {
		return this.filterEnd;
	}

}
