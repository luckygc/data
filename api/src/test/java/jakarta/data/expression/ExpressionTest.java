/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 */
package jakarta.data.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.AtMost;
import jakarta.data.mock.entity.Book;
import jakarta.data.mock.entity._Book;
import jakarta.data.restrict.BasicRestriction;
import jakarta.data.restrict.Restriction;
import jakarta.data.spi.expression.function.NumericFunctionExpression;
import jakarta.data.spi.expression.function.TextFunctionExpression;
import jakarta.data.spi.expression.literal.NumericLiteral;
import jakarta.data.spi.expression.literal.StringLiteral;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class ExpressionTest {

    @Test
    void shouldCompareWithOtherEntityAttribute() {
        Restriction<Book> autobiographies =
                _Book.title.equalTo(_Book.author);

        @SuppressWarnings("unchecked")
        BasicRestriction<Book, String> restriction =
                (BasicRestriction<Book, String>) autobiographies;

        EqualTo<String> equalToConstraint =
                (EqualTo<String>) restriction.constraint();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(restriction.expression())
                .isEqualTo(_Book.title);

            soft.assertThat(equalToConstraint.expression())
                .isEqualTo(_Book.author);
        });
    }

    @Test
    void shouldRestrictLast2ofFirst10Chars() {

        Restriction<Book> titleWithEE =
            _Book.title.left(10).right(2).upper().equalTo("EE");

        @SuppressWarnings("unchecked")
        BasicRestriction<Book, String> restriction =
            (BasicRestriction<Book, String>) titleWithEE;

        EqualTo<String> constraint = (EqualTo<String>) restriction.constraint();
        StringLiteral literal = (StringLiteral) constraint.expression();

        TextFunctionExpression<Book> upperExpression =
            (TextFunctionExpression<Book>) restriction.expression();

        List<? extends Expression<? super Book, ?>> upperArgs = upperExpression.arguments();
        assertEquals(1, upperArgs.size());

        @SuppressWarnings("unchecked")
        TextFunctionExpression<Book> rightExpression =
            (TextFunctionExpression<Book>) upperArgs.getFirst();

        List<? extends Expression<? super Book, ?>> rightArgs = rightExpression.arguments();
        assertEquals(2, rightArgs.size());

        assertInstanceOf(NumericLiteral.class, rightArgs.get(1));

        @SuppressWarnings("unchecked")
        NumericLiteral<Integer> rightArg1 =
            (NumericLiteral<Integer>) rightArgs.get(1);

        @SuppressWarnings("unchecked")
        TextFunctionExpression<Book> leftExpression =
            (TextFunctionExpression<Book>) rightArgs.get(0);

        List<? extends Expression<? super Book, ?>> leftArgs = leftExpression.arguments();
        assertEquals(2, leftArgs.size());

        assertInstanceOf(NumericLiteral.class, leftArgs.get(1));

        @SuppressWarnings("unchecked")
        NumericLiteral<Integer> leftArg1 =
            (NumericLiteral<Integer>) leftArgs.get(1);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(leftExpression.name())
                .isEqualTo(TextFunctionExpression.LEFT);

            soft.assertThat(leftArgs.getFirst())
                .isEqualTo(_Book.title);

            soft.assertThat(leftArg1.value())
                .isEqualTo(10);

            soft.assertThat(rightExpression.name())
                .isEqualTo(TextFunctionExpression.RIGHT);

            soft.assertThat(rightArg1.value())
                .isEqualTo(2);

            soft.assertThat(upperExpression.name())
                .isEqualTo(TextFunctionExpression.UPPER);

            soft.assertThat(literal.value())
                .isEqualTo("EE");
        });
    }

    @Test
    void shouldRestrictLengthOfText() {

        Restriction<Book> titleUpTo50Chars = _Book.title.length().lessThanEqual(50);

        @SuppressWarnings("unchecked")
        BasicRestriction<Book, Integer> restriction =
            (BasicRestriction<Book, Integer>) titleUpTo50Chars;

        AtMost<Integer> constraint =
            (AtMost<Integer>) restriction.constraint();

        NumericLiteral<?> literal =
            (NumericLiteral<?>) constraint.bound();

        NumericFunctionExpression<Book, Integer> lengthExpression =
            (NumericFunctionExpression<Book, Integer>) restriction.expression();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(lengthExpression.name())
                .isEqualTo(NumericFunctionExpression.LENGTH);

            soft.assertThat(lengthExpression.arguments().size())
                .isEqualTo(1);

            soft.assertThat(lengthExpression.arguments().getFirst())
                .isEqualTo(_Book.title);

            soft.assertThat(literal.value())
                .isEqualTo(50);
        });
    }
}
