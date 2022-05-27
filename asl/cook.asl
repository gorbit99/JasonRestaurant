// Agent sample_agent in project restaurant

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+activeOrder(ID) : true <- do(cookFood, ID).
