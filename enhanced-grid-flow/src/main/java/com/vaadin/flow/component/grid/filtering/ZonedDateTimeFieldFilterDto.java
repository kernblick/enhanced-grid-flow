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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.jpa.domain.Specification;

public class ZonedDateTimeFieldFilterDto<R> implements BackendFilterFieldDto<R, ZonedDateTime> {

	private ZonedDateTime filterStart;
	private ZonedDateTime filterEnd;

	private SingularAttribute<R, ZonedDateTime> specificationField;

	private Function<Root<R>, Expression<ZonedDateTime>> metaclassFunction;

	public ZonedDateTimeFieldFilterDto() {
	}

	public ZonedDateTimeFieldFilterDto(SingularAttribute<R, ZonedDateTime> specificationField) {
		this.specificationField = specificationField;
	}

	public ZonedDateTimeFieldFilterDto(Function<Root<R>, Expression<ZonedDateTime>> metaclassFunction) {
		this.metaclassFunction = metaclassFunction;
	}

	public ZonedDateTimeFieldFilterDto(ZonedDateTime filterStart, ZonedDateTime filterEnd) {
		this.filterStart = filterStart;
		this.filterEnd = filterEnd;
	}

	public ZonedDateTimeFieldFilterDto(SingularAttribute<R, ZonedDateTime> specificationField,
			ZonedDateTime filterStart, ZonedDateTime filterEnd) {
		this(filterStart, filterEnd);
		this.specificationField = specificationField;
	}

	public SingularAttribute<R, ZonedDateTime> getSpecificationField() {
		return this.specificationField;
	}

	public void setSpecificationField(SingularAttribute<R, ZonedDateTime> specificationField) {
		this.specificationField = specificationField;
	}

	public ZonedDateTimeFieldFilterDto<R> specificationField(SingularAttribute<R, ZonedDateTime> specificationField) {
		setSpecificationField(specificationField);
		return this;
	}

	@Override
	public Predicate<ZonedDateTime> getFilterPredicate() {
		Predicate<ZonedDateTime> simplePredicate = s -> true;

		if (filterStart != null) {
			simplePredicate = simplePredicate.and(s -> s.isAfter(filterStart.minusNanos(1)));
		}

		if (filterEnd != null) {
			simplePredicate = simplePredicate.and(s -> s.isBefore(filterEnd.plusNanos(1)));
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
			Function<Root<R>, Expression<ZonedDateTime>> expression = r -> r.get(specificationField.getName());
			spec = spec.and(buildSpecificationFromField(expression));
		}

		// only a specifcation given
		if (metaclassFunction != null) {
			spec = spec.and(buildSpecificationFromField(metaclassFunction));
		}

		return spec;
	}

	private Specification<R> buildSpecificationFromField(
			Function<Root<R>, Expression<ZonedDateTime>> metaclassFunction) {
		Specification<R> spec = Specification.where(null);

		ZonedDateTime from = filterStart != null ? filterStart
				: ZonedDateTime.ofInstant(Instant.MIN, ZoneId.systemDefault());
		ZonedDateTime to = filterEnd != null ? filterEnd : ZonedDateTime.ofInstant(Instant.MAX, ZoneId.systemDefault());

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
		if (!(o instanceof ZonedDateTimeFieldFilterDto)) {
			return false;
		}
		ZonedDateTimeFieldFilterDto zonedDateTimeFieldFilterDto = (ZonedDateTimeFieldFilterDto) o;
		return Objects.equals(filterStart, zonedDateTimeFieldFilterDto.filterStart)
				&& Objects.equals(filterEnd, zonedDateTimeFieldFilterDto.filterEnd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(filterStart, filterEnd, specificationField, metaclassFunction);
	}

	public LocalDateTime getFilterStart() {
		return Optional.ofNullable(filterStart).map(ZonedDateTime::toLocalDateTime).orElse(null);
	}

	public void setFilterStart(LocalDateTime filterStart) {
		this.filterStart = Optional.ofNullable(filterStart).map(f -> f.atZone(ZoneId.systemDefault())).orElse(null);
	}

	public LocalDateTime getFilterEnd() {
		return Optional.ofNullable(filterEnd).map(ZonedDateTime::toLocalDateTime).orElse(null);
	}

	public void setFilterEnd(LocalDateTime filterEnd) {
		this.filterEnd = Optional.ofNullable(filterEnd).map(f -> f.atZone(ZoneId.systemDefault())).orElse(null);
	}

	public void setDateFilterStart(LocalDate filterStart) {
		this.filterStart = Optional.ofNullable(filterStart).map(f -> f.atStartOfDay(ZoneId.systemDefault()))
				.orElse(null);
	}

	public LocalDate getDateFilterStart() {
		return Optional.ofNullable(filterStart).map(ZonedDateTime::toLocalDate).orElse(null);
	}

	public void setDateFilterEnd(LocalDate filterEnd) {
		this.filterEnd = Optional.ofNullable(filterEnd)
				.map(f -> f.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1)).orElse(null);
	}

	public LocalDate getDateFilterEnd() {
		return Optional.ofNullable(filterEnd).map(ZonedDateTime::toLocalDate).orElse(null);
	}

}
