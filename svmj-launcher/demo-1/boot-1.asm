; demo 1 boot

; this asm is writen for a vm with 20 registers

; a boot loader must fit within 1 ram page

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

; these symbols have to start at '0'
; they are used by the bios to
; look up the functions and 
; data it requires to invoke us
_externalSymbolBase:
; size of image in bytes (must be page aligned)
; minium size 1 page
_extSize:
  .4byte 2000H
; location of the entry function
_extEntry:
  .4byte 404cH

; globals declared by bios.asm
_biosSymbolBase:
_biosRamPhysicalBaseAddress:
  .4byte 4H
_biosRamPhysicalSize:
  .4byte 8H
_biosRamPageSize:
  .4byte cH
_biosPageDirectoryEntrySize:
  .4byte 10H
_biosPageDirectoryEntryAddressMask:
  .4byte 14H
_biosPageDirectoryIndexMask:
  .4byte 18H
_biosPageTableEntrySize:
  .4byte 1cH
_biosPageTableEntryAddressMask:
  .4byte 20H
_biosPageTableIndexMask:
  .4byte 24H
_biosFrameAllocatorMark:
  .4byte 28H
_biosFrameAllocatorClear:
  .4byte 5cH
_biosFrameAllocatorCheck:
  .4byte 94H
_biosFrameAllocatorFirstFree:
  .4byte d8H
_biosMapReadPageEntry:
  .4byte 178H
_biosMapWritePageEntry:
  .4byte 1b0H
_biosMapPageRange:
  .4byte 1ecH

_systemCallTest:
  ENTER

  ; privilege ring 0
  NO_OP

  LEAVE
  RETURN

_systemFastCallEntry:
  ; we do not setup a stack frame
  ; for this symbol as we need
  ; to manually control the transitions
  
  ; r4 contains system call number
  
  ; set register for kernel
  
  ; ...
  
  ; write kernel stack to r21
  ; this should be priveged we need to update
  ; the vm to protect the controll registers
  MOVE r2 r21
  
  ; set return locations
  ; return value is r4 if there was one
  ; do not change r4
  ; put user space program counter in r5
  ; put user space stack pointer in r6
  
  ; flush the tlb to protect the kernel
  
  SYSTEM_EXIT

_systemCallStub:
  ENTER
  CR_TO_GR c3 r3
  
  ; we need to save the return address
  ; and the stack pointer for the fast call
  ; entry function to restore our state
  
  LOAD_Y r4 r3  8; argument 1 (kernel function to call)
  SYSTEM_ENTER
_systemCallStubReturn:
  
  LEAVE
  RETURN

_systemCallTestStub:
  ENTER
  
  MOVE_I r5 ah ; system call 10
  PUSH r5
  CALL_I _systemCallStub
  
  LEAVE
  RETURN

_load:
  ENTER
  
  PUSH r0
  CALL_I _systemCallTestStub
  
  LEAVE
  RETURN
