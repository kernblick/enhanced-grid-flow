package com.vaadin.flow.component.grid;

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


import javax.validation.constraints.NotNull;

import com.vaadin.componentfactory.enhancedgrid.EnhancedGrid;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import org.springframework.data.jpa.domain.Specification;

/**
 * Custom event triggered by enhanced-grid-sorter element.
 *
 */
@DomEvent("filter-applied")
public class FilterAppliedEvent<T> extends ComponentEvent<EnhancedGrid<T>> {

	@NotNull
	public final Specification<T> specification;

	public FilterAppliedEvent(EnhancedGrid<T> source, boolean fromClient, @EventData("event.detail.specification") Specification<T> specification) {
		super(source, fromClient);
		this.specification = specification;
	}
}
