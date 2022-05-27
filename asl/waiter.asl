// Agent sample_agent in project restaurant

last_dir(null). // the last movement I did

!waitingForGuest.

+!next_step(X,Y)
   :  pos(AgX,AgY)
   <- jia.get_direction(AgX, AgY, X, Y, D);
      //.print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
      -+last_dir(D);
      do(D).
+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true  // failure handling -> start again!
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      -+last_dir(null);
      !next_step(X,Y).

/* Plans */

+!waitingForGuest : foodOnCounter(ID, X, Y) <- !pickUpFood(ID, X, Y).
+!waitingForGuest : waitingToPay(ID, X, Y) <- !bringBill(X, Y).
+!waitingForGuest : waitingToOrder(_,X,Y) <- !next_step(X, Y); !takeOrder(X, Y).
+!waitingForGuest : not waitingToOrder(_,X,Y) <- !waitingForGuest.

+!takeOrder(X, Y) : cell(DX, DY, debry) & not pos(DX, DY)
    <- !next_step(DX, DY); !takeOrder(X, Y).
+!takeOrder(X, Y) : cell(DX, DY, debry) & pos(DX, DY)
    <- do(cleanDebry); !takeOrder(X, Y).
+!takeOrder(X, Y) : not pos(X, Y) <- !next_step(X, Y); !takeOrder(X, Y).
+!takeOrder(X, Y) : pos(X, Y) <- do(takeOrder); !waitingForGuest.

+!pickUpFood(X, Y) : cell(DX, DY, debry) & not pos(DX, DY)
    <- !next_step(DX, DY); !pickUpFood(X, Y).
+!pickUpFood(X, Y) : cell(DX, DY, debry) & pos(DX, DY)
    <- do(cleanDebry); !pickUpFood(X, Y).
+!pickUpFood(ID, X, Y) : not pos(X, Y) 
    <- !next_step(X, Y); !pickUpFood(ID, X, Y).
+!pickUpFood(ID, X, Y) : waitingForFood(ID, CX, CY) 
    <- do(pickUpFood); !serveFood(CX, CY).

+!serveFood(X, Y) : cell(DX, DY, debry) & not pos(DX, DY)
    <- !next_step(DX, DY); !serveFood(X, Y).
+!serveFood(X, Y) : cell(DX, DY, debry) & pos(DX, DY)
    <- do(cleanDebry); !serveFood(X, Y).
+!serveFood(X, Y) : not pos(X, Y) <- !next_step(X, Y); !serveFood(X, Y).
+!serveFood(X, Y) : pos(X, Y) <- do(serveFood); !waitingForGuest.

+!bringBill(X, Y) : cell(DX, DY, debry) & not pos(DX, DY)
    <- !next_step(DX, DY); !bringBill(X, Y).
+!bringBill(X, Y) : cell(DX, DY, debry) & pos(DX, DY)
    <- do(cleanDebry); !bringBill(X, Y).
+!bringBill(X, Y) : not pos(X, Y) <- !next_step(X, Y); !bringBill(X, Y).
+!bringBill(X, Y) : pos(X, Y) <- do(takePayment); !waitingForGuest.
