; demo 1 bios

; this asm is writen for a vm with 20 registers

; Registers
;  0 - Program Counter
;  1 - Page Directory Base Pointer (this must be a physical memory address)
;  2 - Stack Pointer
;  3 - Base Pointer
;  4 - subroutine return value
; 18 - inturrupt handler table
; 19 - page fualt address

; **************************************
; Page Frame
; **************************************
; RAM is allocated by page frame which is
; the same size as a virtual page

; **************************************
; Virtual Paging
; **************************************
; Page directory and page table entries have the exact same format
; this BIOS requires a recursive page directory entry at index 1023
;
; xxxxxxxxxx yyyyyyyyyy zzzzzzzzzzzz
;
; x - Page Directory index
; y - Page Table index
; z - page offset

; **************************************
; Calling Convention
; **************************************
; it is the callers job to protect state before
; calling a subroutine

  ;HALT
  JUMP_I 56cH
  
_ramPhysicalBaseAddress:
  .4byte 1F000000H
_ramPhysicalSize:
  .4byte 200000h
_ramPageSize:
  .4byte 1000h
_pageDirectoryEntrySize:
  .4byte 4h
_pageDirectoryEntryAddressMask:
  .4byte FFFFF000h
_pageDirectoryIndexMask:
  .4byte FFC00000h
_pageTableEntrySize:
  .4byte 4h
_pageTableEntryAddressMask:
  .4byte FFFFF000h
_pageTableIndexMask:
  .4byte 3FF000h
_boot1BaseAddress:
  .4byte 4000h

; function
; argument 1, base address of frame table
; argument 2, index of frame
_frameAllocatorMark:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3  8     ; argument 1, base address of frame table
  LOAD_Y r9 r3 12     ; argument 2, index of frame
  MOVE_I r4 20h       ; element size is 32 bits 
  DIVIDE r9 r4 r10    ; divide index by element size in bits
  MOD r9 r4 r11       ; mod index by element size in bits
  MOVE_I r5 1h
  SHIFT_LEFT r5 r11   ; calc mask for bit
  LOAD_4X r9 r8 r10   ; read current element value
  OR r9 r5 r12        ; mark the bit for the index
  STORE_4X r12 r8 r10  ; write back the element
  LEAVE
  RETURN

; function
; argument 1, base address of frame table
; argument 2, index of frame
_frameAllocatorClear:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3  8     ; argument 1, base address of frame table
  LOAD_Y r9 r3 12     ; argument 2, index of frame
  MOVE_I r4 20h       ; element size is 32 bits 
  DIVIDE r9 r4 r10    ; divide index by element size in bits
  MOD r9 r4 r11       ; mod index by element size in bits
  MOVE_I r5 1h
  SHIFT_LEFT r5 r11   ; calc mask for bit
  NOT r5 r5
  LOAD_4X r9 r8 r10   ; read current element value
  AND r9 r5 r12       ; mark the bit for the index
  STORE_4X r12 r8 r10 ; write back the element
  LEAVE
  RETURN

; function
; argument 1, base address of frame table
; argument 2, index of frame
; return value in r4 (page frame index status) 0: False, 1: True
_frameAllocatorCheck:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3  8     ; argument 1, base address of frame table
  LOAD_Y r9 r3 12     ; argument 2, index of frame
  MOVE_I r4 20h       ; element size is 32 bits 
  DIVIDE r9 r4 r10    ; divide index by element size in bits
  MOD r9 r4 r11       ; mod index by element size in bits
  MOVE_I r5 1h
  SHIFT_LEFT r5 r11   ; calc mask for bit
  LOAD_4X r9 r8 r10   ; read current element value
  AND r9 r5 r12       ; extract the bit for the index
  CMP_Z r12           ; compare to zero
  JUMP_NZ_Y 8         ; jump forword pc relative 2 instruction
  MOVE_I r4 1h        ; return true
  JUMP_Y 4            ; jump forword pc relative 1 instruction
  MOVE_I r4 0h        ; return false
  LEAVE
  RETURN

; we need to handle 0 free page frames
;
; function
; argument 1 (page frame bitmap base address) should be page aligned
; return value in r4 (page frame index)
_frameAllocatorFirstFree:
  ENTER
  LOAD_I r15 _ramPhysicalSize
  LOAD_I r14 _ramPageSize
  DIVIDE r15 r14 r15   ; calc page frame count
  CR_TO_GR c3 r3
  LOAD_Y r14 r3  8     ; argument 1, base address of frame table
  MOVE_I r13 0h        ; frame index
_frameAllocatorFirstFreeLoop1:
  PUSH r13          ; push frame index
  PUSH r14          ; push frame table base address
  CALL_I _frameAllocatorCheck
                       ; return value is in r4
  CMP_Z r4
  JUMP_NZ_Y 12
  INCREMENT r13
  CMP r15 r13
  JUMP_NZ_I _frameAllocatorFirstFreeLoop1
  MOVE r13 r4          ; put return value in r4
  LEAVE
  RETURN

; ** NOTE **
; to be able to use this subroutine when paging is enabled
; we must map a virtual address range to the plysical address
; range at the exact same location
; ** NOTE 2 **
; this is a privileged action and in the future will need to be
; protected so a lower privileged process cannot write to the
; page direcotries or tables
;
; this function will only work when paging is turned off
; or when the page directory is identity mapped
;
; function
; argument 1 (pd index)
; argument 2 (page table entry)
_writePageDirectoryEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3  8                   ; argument 1 (pd index)
  LOAD_Y r9 r3 12                   ; argument 2 (page table entry)
  CR_TO_GR c1 r1
  STORE_4X r9 r1 r8
  LEAVE
  RETURN

; function
;
; this function will only work when paging is turned off
; or when the page directory is identity mapped
;
; argument 1 (pde index)
; return value in r4 (page directory entry)
_readPageDirectoryEntry:
  ENTER
  CR_TO_GR c3 r3
  CR_TO_GR c1 r1
  LOAD_Y r8 r3  8        ; argument 1 (page directory index)
  LOAD_4X r4 r1 r8       ; read page entry at index (r8) with base address (r1) (r4=[r1+r8*4])
  LEAVE
  RETURN

; function
;
; this function will only work when paging is turned off
; or when the page directory is identity mapped
;
; argument 1 (pde index)
_writePageTableEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3 8                    ; argument 1 (pde index)
  PUSH r8
  CALL_I _readPageDirectoryEntry
  
  ; return value is in r4
  LOAD_I r5 _pageDirectoryEntryAddressMask
  AND r4 r5 r6

  CR_TO_GR c3 r3
  LOAD_Y  r9 r3  12                 ; argument 2 (pte index)
  LOAD_Y r10 r3  16                 ; argument 3 (pte)
  MOVE_I r5 1h
  STORE_4X r10 r6 r9                ; r10 value to store r6 base address r9 index ([r6+r9*4]=r10)

  LEAVE
  RETURN

; function
;
; this function requires a recursively mapped
; page directory entry at index 1023
;
; argument 1 (page directory index)
; argument 2 (page table index)
_mapReadPageEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r12 r3  8; argument 1
  LOAD_Y r13 r3 12; argument 2
  
  ; recursive look up
  MOVE_I r15 0h
  MOVE_I r16 3FFh
  MOVE_I r17 16h
  SHIFT_LEFT r16 r17
  OR r15 r16 r15
  
  ; or in the page directory index
  MOVE_I r17 ch
  SHIFT_LEFT r12 r17
  OR r15 r12 r15
  
  ; use base + index * 4 instruction to read entry
  LOAD_4X r4 r15 r13
  
  ; return value in r4
  
  LEAVE
  RETURN

; function
;
; this function requires a recursively mapped
; page directory entry at index 1023
;
; argument 1 (page directory index)
; argument 2 (page table index)
; argument 3 (virtual page entry)
_mapWritePageEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r12 r3  8; argument 1
  LOAD_Y r13 r3 12; argument 2
  LOAD_Y r14 r3 16; argument 3
  
  ; recursive look up
  MOVE_I r15 0h
  MOVE_I r16 3FFh
  MOVE_I r17 16h
  SHIFT_LEFT r16 r17
  OR r15 r16 r15
  
  ; or in the page directory index
  MOVE_I r17 ch
  SHIFT_LEFT r12 r17
  OR r15 r12 r15
  
  ; use base + index * 4 instruction to write entry
  STORE_4X r14 r15 r13
  
  LEAVE
  RETURN

; function
;
; this function requires a recursively mapped
; page directory entry at index 1023
;
; argument 1 (physical address block start) must be page aligned
; argument 2 (virtual address block start) must be page aligned
; argument 3 (size of block in bytes) must be a multiple of a page

; LOAD_Y  r* r3  8 ; physical start
; LOAD_Y  r* r3 12 ; virtual start
; LOAD_Y  r* r3 16 ; size
_mapPageRange:
  ENTER
  MOVE_I r5 0h
  PUSH r5      ; LOAD_Y r* r3 -4, local variable 1 ( index )
  
  ; calculate page count
  CR_TO_GR c3 r3
  LOAD_Y r7 r3 16 ; size
  LOAD_I r8 _ramPageSize
  DIVIDE r7 r8 r7
  PUSH r7      ; LOAD_Y r* r3 -8, local variable 2 ( page count )
  
_mapPageRangeLoop1Start:
  ; calculate page directory index
  CR_TO_GR c3 r3
  LOAD_I r7 _ramPageSize
  LOAD_Y r6 r3 -4 ; index
  MULTIPLY r6 r7 r7
  
  LOAD_Y r6 r3 12
  ADD r6 r7 r6
  
  LOAD_I r7 _pageDirectoryIndexMask
  AND r6 r7 r6
  MOVE_I r7 16h
  SHIFT_RIGHT r6 r7
  
  PUSH r6
  MOVE_I r4 3FFh
  PUSH r4
  CALL_I _mapReadPageEntry
  ; return value in r4
  POP r6
  POP r6 ; restore page directory index to r6
  MOVE r4 r17
  
  MOVE_I r8 1h
  AND r17 r8 r8
_mapPageRangeJMP1:
  JUMP_NZ_Y 80 ; if r8 is 0 we need a new page table
  
  ; get a free page frame of ram to create a 
  ; new page table on
  LOAD_I r4 _ramPhysicalBaseAddress  ; address of the page frame allocator bit map
  PUSH r4                         ; push argument on the stack
  CALL_I _frameAllocatorFirstFree
  
  PUSH r4
  LOAD_I r8 _ramPhysicalBaseAddress
  PUSH r8
  CALL_I _frameAllocatorMark
  POP r4
  POP r4 ; restore r4 to the index of the page frame
  
  ; use as the frame index (r4 * page size + ram base)
  LOAD_I r8 _ramPageSize
  MULTIPLY r4 r8 r9
  LOAD_I r8 _ramPhysicalBaseAddress
  ADD r9 r8 r9 ; contains the address of the ram frame to use for the new page table
  MOVE_I r8 1h
  OR r9 r8 r9 ; mark the entry as valid
  
  PUSH r9
  PUSH r6
  MOVE_I r8 3FFh
  PUSH r8
  CALL_I _mapWritePageEntry
  
_mapPageRangeJMP1NZ:
  ; calculate page table index  
  CR_TO_GR c3 r3
  LOAD_I r7 _ramPageSize
  LOAD_Y r8 r3 -4 ; index
  MULTIPLY r8 r7 r7

  LOAD_Y r5 r3 12
  ADD r5 r7 r5
  
  LOAD_I r7 _pageTableIndexMask
  AND r5 r7 r5
  MOVE_I r7 ch
  SHIFT_RIGHT r5 r7
  
  LOAD_Y r12 r3 -4 ; load index
  LOAD_I r13 _ramPageSize
  MULTIPLY r12 r13 r12
  LOAD_Y  r8 r3 8
  ADD r8 r12 r8
  MOVE_I r13 1h
  OR r8 r13 r8

  PUSH r8
  PUSH r5
  PUSH r6
  CALL_I _mapWritePageEntry
  
  CR_TO_GR c3 r3
  LOAD_Y r12 r3 -4 ; load index
  INCREMENT r12
  STORE_Y r12 r3 -4
  LOAD_Y r13 r3 -8 ; load index
  CMP r12 r13
  JUMP_NZ_I _mapPageRangeLoop1Start
  
  LEAVE
  RETURN

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

_runMapBoot1:
  ENTER
  
  ; in the future we need to give the bios a way
  ; to search for devices on the memory address buss
  
  ; we cannot read the boot information table
  ; because it's address space is not mapped
  ; we will map a single page first at the start of the device
  ; and then we will do the dynamic mapping
  
  LOAD_I r5 _boot1BaseAddress
  LOAD_I r6 _ramPageSize
  PUSH r6
  PUSH r5
  PUSH r5
  CALL_I _mapPageRange
  
  LOAD_I r5 _boot1BaseAddress
  MOVE_I r8 0h                 ; boot external symbol index 0
  LOAD_4X r6 r5 r8             ; load index 0, image size in bytes page aligned
  PUSH r6
  PUSH r5
  PUSH r5
  CALL_I _mapPageRange

  LEAVE
  RETURN

; run a user program, in the future this would be a boot loader
_run:
  ENTER

  CALL_I _runMapBoot1
  
  LOAD_I r5 _boot1BaseAddress
  MOVE_I r8 1h                 ; boot external symbol index 1
  LOAD_4X r6 r5 r8             ; load index 1, entry function
  
  HALT
  CALL r6
  
  LEAVE
  RETURN

_start:
  ; fixup stack as it is currently incorrect
  LOAD_I r4 _ramPhysicalBaseAddress
  
  ; the vm starts with the base pointer set to the physical size of ram
  CR_TO_GR c3 r5
  ADD r4 r5 r5  
  GR_TO_CR r5 c3
  
  ; the vm starts with the stack pointer set to the physical size of ram
  CR_TO_GR c2 r5
  ADD r4 r5 r5
  GR_TO_CR r5 c2
  
  CALL_I _init
  
  ; when init returns we are running in paged mode
  
  CALL_I _run
  
  HALT
