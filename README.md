# Monads
A demo repository to show the power of Monadic style coding. This repo is part of my tech talk: Feng Shui your Java projects with Monadic style coding.

## Context
This repo is in the context of a fictional company that operates in the financial services industry. The company offers a variety of financial products, including life insurance, mortgages, pension plans, as well as non-life insurance options such as travel, car, and fire insurance.

Annually, the company computes a discount tailored to each customer, determined by the products held by the customer. Each product type has its own unique set of business rules that are used to decide the discount for that type of product. That's where this codebase comes into action.

## Code structure

This codebase centers around the _domain_ package, which houses the product package containing various product definitions, as well as the ```DiscountApi``` interface. Initially, the DiscountApi was implemented by the ```DiscountService``` using a conventional imperative approach. To offer an alternative, I've introduced a new implementation using a Monadic style programming approach, which is available within the ```DiscountServiceMonadized``` class.

To demonstrate the functionality of this monadic approach, I've incorporated two Spring profiles into the project: ```@Profile("monadless")``` and ```@Profile("monadic")```. These profiles facilitate the wiring of either the _DiscountService_ or the _DiscountServiceMonadized_, accordingly. I've specifically employed this setup in the DiscountServiceTests class to verify whether the behavior of the former implementation aligns with that of the new monadic implementation.

