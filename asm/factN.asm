			ALLOC 2
			MOV 1,10
			MOV 2,1
jump_here:	MUL 2,#1
			DEC 1
			TEST 1,#1
			JNZ jump_here
			RETURN #2