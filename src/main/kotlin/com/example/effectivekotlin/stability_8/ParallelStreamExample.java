package com.example.effectivekotlin.stability_8;

import java.util.Arrays;
import java.util.List;

public class ParallelStreamExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // 순차 스트림 사용
        int sumSequential = numbers.stream()
                .map(n -> n * n) // 각 숫자를 제곱
                .reduce(0, Integer::sum); // 합계 구하기

        System.out.println("Sequential sum: " + sumSequential);

        // 병렬 스트림 사용
        int sumParallel = numbers.parallelStream()
                .map(n -> n * n) // 각 숫자를 제곱
                .reduce(0, Integer::sum); // 합계 구하기

        System.out.println("Parallel sum: " + sumParallel);
    }
}
