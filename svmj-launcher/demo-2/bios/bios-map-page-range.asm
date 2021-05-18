
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
.function _mapPageRange
.global   _mapPageRange
.size     _mapPageRange $ - _mapPageRange

.global _ramPageSize
.global _ramPhysicalBaseAddress
.global _mapReadPageEntry
.global _mapWritePageEntry
.global _pageDirectoryIndexMask
.global _pageTableIndexMask
.global _frameAllocatorFirstFree
.global _frameAllocatorMark

