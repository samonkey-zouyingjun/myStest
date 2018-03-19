/*
 * Copyright 2018 Google Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.evideo.sambaprovider.thread;

import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * {@link LinkedBlockingDeque} using LIFO algorithm
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.6.3
 */
public class LifoLinkedBlockingDeque<E> extends LinkedBlockingDeque<E> {

    private static final long serialVersionUID = 2737089814284018754L;
    
    /**
     * Inserts the specified element at the front of this deque if it is possible to do so immediately without violating
     * capacity restrictions, returning <tt>true</tt> upon success and <tt>false</tt> if no space is currently
     * available. When using a capacity-restricted deque, this method is generally preferable to the {@link #addFirst
     * addFirst} method, which can fail to insert an element only by throwing an exception.
     * 
     * @param e
     *            the element to add
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             if the specified element is null
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     */
    @Override
    public boolean offer(E e) {
        return super.offerFirst(e);
    }

    /**
     * Retrieves and removes the first element of this deque. This method differs from {@link #pollFirst pollFirst} only
     * in that it throws an exception if this deque is empty.
     * 
     * @return the head of this deque
     * @throws NoSuchElementException
     *             if this deque is empty
     */
    @Override
    public E remove() {
        return super.removeFirst();
    }

}
