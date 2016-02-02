			ALLOC 2			// allocate 2 registers
			MOV 1,3		// reg1 = 10
			MOV 2,1			// reg2 = 1
jump_here:	MUL 2,#1		// reg2 *= reg1
			DEC 1			// reg1--
			TEST 1,#1		// check reg1, set flags (zero flag)
			JNZ &jump_here	// jump if not zero
			RETURN #2