		ALLOC 2
		MOV 1 10	// r1 = 10
here:	ADD 2 #1	// r2 += r1
		DEC 1		// r1--
		TEST 1 #1	// if (r1 != 0)
		JNZ here	// 	jmp 9
		RETURN #2