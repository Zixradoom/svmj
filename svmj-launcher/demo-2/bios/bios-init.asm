
; initialize the base system
_init:
  ENTER
  ; **********************************************
  ;
  ; **NOTE** this will change if the size of ram
  ;   changes
  ;
  ; MARK the implicit page frames 
  ;   RAM
  ;     page frame 1 (0x1)       page frame allocator bit map
  ;     page frame 511 (0x1FF)   stack
  ;
  ; **********************************************
  
  MOVE_I r5 0h
  PUSH r5
  LOAD_I r4 _ramPhysicalBaseAddress
  PUSH r4
  CALL_I _frameAllocatorMark
  
  MOVE_I r5 1ffh
  PUSH r5
  LOAD_I r4 _ramPhysicalBaseAddress
  PUSH r4
  CALL_I _frameAllocatorMark
  
  ; **********************************************
  ;
  ; SETUP THE PAGE DIRECTORY for the BIOS
  ;
  ; **********************************************
  
  ; get a free page frame of ram to use for the 
  ; BIOS Page Directory
  LOAD_I r4 _ramPhysicalBaseAddress  ; address of the page frame allocator bit map
  PUSH r4                         ; push argument on the stack
  CALL_I _frameAllocatorFirstFree
  
  MOVE r4 r6
  
  ; mark frame in r4 as in use
  ; BIOS Page Directory 
  PUSH r4
  LOAD_I r5 _ramPhysicalBaseAddress
  PUSH r5
  CALL_I _frameAllocatorMark
  
  MOVE r6 r4
  
  ; calculate the page frame address based on the page frame index returned
  ; by _frameAllocatorFirstFree (r4)
  
  ; use as the frame index (r4 * page size + ram base)
  LOAD_I r5 _ramPageSize
  MULTIPLY r4 r5 r6
  LOAD_I r4 _ramPhysicalBaseAddress
  ADD r6 r4 r6

  ; store the BIOS Page Directory address
  ; in r1 the Page Directory pointer register
  GR_TO_CR r6 c1

  ; recursive map entry index 1023 to the page directory
  MOVE_I r5 1h
  CR_TO_GR c1 r1
  OR r1 r5 r13        ; create pd entry and mark it valid
  PUSH r13         ; push pd entry on the stack
  MOVE_I r12 3FFh     ; pd index
  PUSH r12         ; push pd entry on the stack
  CALL_I _writePageDirectoryEntry

  ; **********************************************
  ;
  ; IDENTITY MAP THE BIOS into virtual memory
  ;
  ; **********************************************

  ; get a free page frame of ram to use for the 
  ; first page table
  LOAD_I r5 _ramPhysicalBaseAddress
  PUSH r5
  CALL_I _frameAllocatorFirstFree

  ; mark frame in r4 as in use
  PUSH r4
  LOAD_I r5 _ramPhysicalBaseAddress
  PUSH r5
  CALL_I _frameAllocatorMark
  POP r4
  POP r4

  ; calculate frame address
  LOAD_I r5 _ramPageSize
  MULTIPLY r4 r5 r6
  LOAD_I r5 _ramPhysicalBaseAddress
  ADD r6 r5 r6
  MOVE_I r4 1h
  OR r6 r4 r6    ; mark page entry valid

  ; identity map physical [0x0,0xFFF] to virtual [0x0,0xFFF] 
  MOVE_I r8 0h    ; Page Directory index 0
  
  PUSH r6      ; r4 already holds the address of page table 0
  PUSH r8
  CALL_I _writePageDirectoryEntry
  
  MOVE_I r8 0h    ; pd index 0
  MOVE_I r9 0h    ; pt index 0
  MOVE_I r10 1h   ; pte (physical ram address 0, and valid)
  PUSH r10
  PUSH r9
  PUSH r8
  CALL_I _writePageTableEntry

  ; **********************************************
  ;
  ; IDENTITY MAP THE FIRST 16KiB of RAM into virtual memory
  ; this location holds our current BIOS page directory
  ; and the first few page table entries
  ;
  ; **********************************************

  ; get a free page frame of ram
  LOAD_I r5 _ramPhysicalBaseAddress
  PUSH r5
  CALL_I _frameAllocatorFirstFree

  ; mark frame in r4 as in use
  PUSH r4
  LOAD_I r5 _ramPhysicalBaseAddress
  PUSH r5
  CALL_I _frameAllocatorMark
  POP r4
  POP r4

  ; calculate frame address
  LOAD_I r5 _ramPageSize
  MULTIPLY r4 r5 r6
  LOAD_I r5 _ramPhysicalBaseAddress
  ADD r6 r5 r6
  MOVE_I r4 1h
  OR r6 r4 r7    ; pte in r7 mark page entry valid

  PUSH r7      ; entry
  MOVE_I r8 7Ch   ; pd index 7Ch
  PUSH r8
  CALL_I _writePageDirectoryEntry
  
  ; identity map physical [0x1F000000,0x1F003FFF] to virtual [0x1F000000,0x1F003FFF]
  ; changed to only the first page of ram
  ; currently page frame 0 is occupied by the page frame allocator bitmap
  ; the pages do not need to be mapped to be used because of a recursive page directory entry
  
  LOAD_I r14 _ramPageSize
  LOAD_I r15 _ramPhysicalBaseAddress
  
  MOVE_I r12 7Ch   ; pd index 7Ch
  MOVE_I r13 1h    ; pt index 4
  MULTIPLY r13 r14 r11
  ADD r11 r15 r11
_mapRangeRAM1LoopStart:
  SUBTRACT r11 r14 r11
  DECREMENT r13
  MOVE_I r5 1h
  OR r5 r11 r5
  PUSH r5           ; pte value
  PUSH r13          ; pt index
  PUSH r12          ; pd index
  CALL_I _writePageTableEntry
  CMP_Z r13
  JUMP_NZ_I _mapRangeRAM1LoopStart

  ; **********************************************
  ;
  ; IDENTITY MAP THE BIOS Stack into virtual memory
  ;
  ; **********************************************

  ; identity map physical [0x1F1FF000,0x1F1FFFFFF] to virtual [0x1F1FF000,0x1F1FFFFFF]

  MOVE_I r8  7Ch    ; pd index 7c
  MOVE_I r9 1FFh    ; pt index 1ff
  LOAD_I r10 _ramPhysicalSize   ; physical ram address
  LOAD_I r11 _ramPageSize
  LOAD_I r12 _ramPhysicalBaseAddress
  SUBTRACT r10 r11 r10
  ADD r10 r12 r10
  MOVE_I r5 1h
  OR r10 r5 r10
  PUSH r10
  PUSH r9
  PUSH r8
  CALL_I _writePageTableEntry

; enable paging
; we currently do not handle
; page faults so we are limited
; in what we can do with paging
; for the time being
  
  ENABLE_PAGING

  LEAVE
  RETURN
.function _init
.global   _init
.size     _init $ - _init

.global _ramPhysicalSize
.global _ramPageSize
.global _ramPhysicalBaseAddress
.global _writePageTableEntry
.global _writePageDirectoryEntry
.global _frameAllocatorFirstFree
.global _frameAllocatorMark
