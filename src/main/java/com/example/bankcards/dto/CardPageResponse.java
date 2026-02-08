package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO для передачи постраничного списка карт пользователя.
 */
@Getter
@Setter
@AllArgsConstructor
public class CardPageResponse
{
    /** Список карт на текущей странице **/
    private List<GetCardResponse> cards;

    /** Номер текущей страницы **/
    private int page;

    /** Количество элементов на странице **/
    private int size;

    /** Общее число элементов **/
    private long totalElements;

    /** Общее число страниц **/
    private int totalPages;

    /** Является ли текущая страница первой */
    private boolean first;

    /** Является ли текущая страница последней */
    private boolean last;
}
