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

!start.

/* Plans */

+!waitingForGuest : waitingToOrder(_,X,Y) <- !next_step(X, Y); !takeOrder(X, Y).
+!waitingForGuest : not waitingToOrder(_,X,Y) <- !waitingForGuest.

+!takeOrder(X, Y) : not pos(X, Y) <- !next_step(X, Y); !takeOrder(X, Y).
+!takeOrder(X, Y) : pos(X, Y) <- do(takeOrder); !waitingForGuest.
