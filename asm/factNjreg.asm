			ALLOC 3			// allocate 2 registers
			MOV 1,10		// reg1 = 10
			MOV 3,16
			MOV 2,1			// reg2 = 1
jump_here:	MUL 2,#1		// reg2 *= reg1
			DEC 1			// reg1--
			TEST 1,#1		// check reg1, set flags (zero flag)
			JMP #3 jump_here	// jump if not zero
			RETURN #2