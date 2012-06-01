all: 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 117 118 119 120 121 122 123 124 125 126 127 128 129 130 131 132 133 134 135 136 137 138 139 140 141 142 143 144 145 146 147 148 149 150 151 152 153 154 155 156 157 158 159 160 161 162 163 164 165 166 167 168 169 170 171 172 173 174 175 176 177 178 179 180 181 182 183 184 185 186 187 188 189 190 191 192 193 194 195 196 197 198 199 200 201 202 203 204 205 206 207 208 209 210 211 212 213 214 215 216 217 218 219 220 221 222 223 224 225 226 227 228 229 230 231 232 233 234 235 236 237 238 239 240 241 242 243 244 245 246 247 248 249 250 251 252 253 254 255 256 257 258 259 260 261 262 263 264 265 266 267 268 269 270 271 272 273 274 275 276 277 278 279 280 281 282 283 284 285 286 287 288 289 290 291 292 293 294 295 296 297 298 299 300 301 302 303 304 305 306 307 308 309 310 311 312 313 314 315 316 317 318 319 320 321 322 323 324 325 326 327 328 329 330 331 332 333 334 335 336 337 338 339 340 341 342 343 344 345 346 347 348 349 350 351 352 353 354 355 356 357 358 359 360 361 362 363 364 365 366 367 368 369 370 371 372 373 374 375 376 377 378 379 380 381 382 383 384 385 386 387 388 389 390 391 392 393 394 395 396 397 398 399 400 401 402 403 404 405 406 407 408 409 410 411 412 413 414 415 416 417 418 419 420 421 422 423 424 425 426 427 428 429 430 431 432 433 434 435 436 437 438 439 440 441 442 443 444 445 446 447 448 449 450 451 452 453 454 455 456 457 458 459 460 461 462 463 464 465 466 467 468 469 470 471 472 473 474 475 476 477 478 479 480 481 482 483 484 485 486 487 488 489 490 491 492 493 494 495 496 497 498 499 500 501 502 503 504 505 506 507 508 509 510 511 512 513 514 515 516 517 518 519 520 521 522 523 524 525 526 527 528 529 530 531 532 533 534 535 536 537 538 539 540 541 542 543 544 545 546 547 548 549 550 551 552 553 554 555 556 557 558 559 560 561 562 563 564 565 566 567 568 569 570 571 572 573 574 575 576 577 578 579 580 581 582 583 584 585 586 587 588 589 590 591 592 593 594 595 596 597 598 599 600 601 602 603 604 605 606 607 608 609 610 611 612 613 614 615 616 617 618 619 620 621 622 623 624 625 626 627 628 629 630 631 632 633 634 635 636 637 638 639 640 641 642 643 644 645 646 647 648 649 650 651 652 653 654 655 656 657 658 659 660 661 662 663 664 665 666 667 668 669 670 671 672 673 674 675 676 677 678 679 680 681 682 683 684 685 686 687 688 689 690 691 692 693 694 695 696 697 698 699 700 701 702 703 704 705 706 707 708 709 710 711 712 713 714 715 716 717 718 719 720 721 722 723 724 725 726 727 728 729 730 731 732 733 734 735 736 737 738 739 740 741 742 743 744 745 746 747 748 749 750 751 752 753 754 755 756 757 758 759 760 761 762 763 764 765 766 767 768 769 770 771 772 773 774 775 776 777 778 779 780 781 782 783 784 785 786 787 788 789 790 791 792 793 794 795 796 797 798 799 800 801 802 803 804 805 806 807 808 809 810 811 812 813 814 815 816 817 818 819 820 821 822 823 824 825 826 827 828 829 830 831 832 833 834 835 836 837 838 839 840 841 842 843 844 845 846 847 848 849 850 851 852 853 854 855 856 857 858 859 860 861 862 863 864 865 866 867 868 869 870 871 872 873 874 875 876 877 878 879 880 881 882 883 884 885 886 887 888 889 890 891 892 893 894 895 896 897 898 899 900 901 902 903 904 905 906 907 908 909 910 911 912 913 914 915 916 917 918 919 920 921 922 923 924 925 926 927 928 929 930 931 932 933 934 935 936 937 938 939 940 941 942 943 944 945 946 947 948 949 950 951 952 953 954 955 956 957 958 959 960 961 962 963 964 965 966 967 968 969 970 971 972 973 974 975 976 977 978 979 980 981 982 983 984 985 986 987 988 989 990 991 992 993 994 995 996 997 998 999 1000 1001 1002 1003 1004 1005 1006 1007 1008 1009 1010 1011 1012 1013 1014 1015 1016 1017 1018 1019 1020 1021 1022 1023 1024 1025 1026 1027 1028 1029 1030 1031 1032 1033 1034 1035 1036 1037 1038 1039 1040

0:
	mkdir q1a/alwaysupdate.user.sols;./gjsolver q1a/alwaysupdate.user q1a/alwaysupdate.user.sols 1> q1a/alwaysupdate.user.out 2> q1a/alwaysupdate.user.err 

1:
	mkdir q1a/i1.user.sols;./gjsolver q1a/i1.user q1a/i1.user.sols 1> q1a/i1.user.out 2> q1a/i1.user.err 

2:
	mkdir q1a/i10.user.sols;./gjsolver q1a/i10.user q1a/i10.user.sols 1> q1a/i10.user.out 2> q1a/i10.user.err 

3:
	mkdir q1a/i11.user.sols;./gjsolver q1a/i11.user q1a/i11.user.sols 1> q1a/i11.user.out 2> q1a/i11.user.err 

4:
	mkdir q1a/i12.user.sols;./gjsolver q1a/i12.user q1a/i12.user.sols 1> q1a/i12.user.out 2> q1a/i12.user.err 

5:
	mkdir q1a/i13.user.sols;./gjsolver q1a/i13.user q1a/i13.user.sols 1> q1a/i13.user.out 2> q1a/i13.user.err 

6:
	mkdir q1a/i14.user.sols;./gjsolver q1a/i14.user q1a/i14.user.sols 1> q1a/i14.user.out 2> q1a/i14.user.err 

7:
	mkdir q1a/i15.user.sols;./gjsolver q1a/i15.user q1a/i15.user.sols 1> q1a/i15.user.out 2> q1a/i15.user.err 

8:
	mkdir q1a/i16.user.sols;./gjsolver q1a/i16.user q1a/i16.user.sols 1> q1a/i16.user.out 2> q1a/i16.user.err 

9:
	mkdir q1a/i17.user.sols;./gjsolver q1a/i17.user q1a/i17.user.sols 1> q1a/i17.user.out 2> q1a/i17.user.err 

10:
	mkdir q1a/i18.user.sols;./gjsolver q1a/i18.user q1a/i18.user.sols 1> q1a/i18.user.out 2> q1a/i18.user.err 

11:
	mkdir q1a/i19.user.sols;./gjsolver q1a/i19.user q1a/i19.user.sols 1> q1a/i19.user.out 2> q1a/i19.user.err 

12:
	mkdir q1a/i2.user.sols;./gjsolver q1a/i2.user q1a/i2.user.sols 1> q1a/i2.user.out 2> q1a/i2.user.err 

13:
	mkdir q1a/i20.user.sols;./gjsolver q1a/i20.user q1a/i20.user.sols 1> q1a/i20.user.out 2> q1a/i20.user.err 

14:
	mkdir q1a/i21.user.sols;./gjsolver q1a/i21.user q1a/i21.user.sols 1> q1a/i21.user.out 2> q1a/i21.user.err 

15:
	mkdir q1a/i22.user.sols;./gjsolver q1a/i22.user q1a/i22.user.sols 1> q1a/i22.user.out 2> q1a/i22.user.err 

16:
	mkdir q1a/i23.user.sols;./gjsolver q1a/i23.user q1a/i23.user.sols 1> q1a/i23.user.out 2> q1a/i23.user.err 

17:
	mkdir q1a/i24.user.sols;./gjsolver q1a/i24.user q1a/i24.user.sols 1> q1a/i24.user.out 2> q1a/i24.user.err 

18:
	mkdir q1a/i25.user.sols;./gjsolver q1a/i25.user q1a/i25.user.sols 1> q1a/i25.user.out 2> q1a/i25.user.err 

19:
	mkdir q1a/i26.user.sols;./gjsolver q1a/i26.user q1a/i26.user.sols 1> q1a/i26.user.out 2> q1a/i26.user.err 

20:
	mkdir q1a/i27.user.sols;./gjsolver q1a/i27.user q1a/i27.user.sols 1> q1a/i27.user.out 2> q1a/i27.user.err 

21:
	mkdir q1a/i28.user.sols;./gjsolver q1a/i28.user q1a/i28.user.sols 1> q1a/i28.user.out 2> q1a/i28.user.err 

22:
	mkdir q1a/i29.user.sols;./gjsolver q1a/i29.user q1a/i29.user.sols 1> q1a/i29.user.out 2> q1a/i29.user.err 

23:
	mkdir q1a/i3.user.sols;./gjsolver q1a/i3.user q1a/i3.user.sols 1> q1a/i3.user.out 2> q1a/i3.user.err 

24:
	mkdir q1a/i30.user.sols;./gjsolver q1a/i30.user q1a/i30.user.sols 1> q1a/i30.user.out 2> q1a/i30.user.err 

25:
	mkdir q1a/i4.user.sols;./gjsolver q1a/i4.user q1a/i4.user.sols 1> q1a/i4.user.out 2> q1a/i4.user.err 

26:
	mkdir q1a/i5.user.sols;./gjsolver q1a/i5.user q1a/i5.user.sols 1> q1a/i5.user.out 2> q1a/i5.user.err 

27:
	mkdir q1a/i6.user.sols;./gjsolver q1a/i6.user q1a/i6.user.sols 1> q1a/i6.user.out 2> q1a/i6.user.err 

28:
	mkdir q1a/i7.user.sols;./gjsolver q1a/i7.user q1a/i7.user.sols 1> q1a/i7.user.out 2> q1a/i7.user.err 

29:
	mkdir q1a/i8.user.sols;./gjsolver q1a/i8.user q1a/i8.user.sols 1> q1a/i8.user.out 2> q1a/i8.user.err 

30:
	mkdir q1a/i9.user.sols;./gjsolver q1a/i9.user q1a/i9.user.sols 1> q1a/i9.user.out 2> q1a/i9.user.err 

31:
	mkdir q1a/never.user.sols;./gjsolver q1a/never.user q1a/never.user.sols 1> q1a/never.user.out 2> q1a/never.user.err 

32:
	mkdir q1a/uandi1.user.sols;./gjsolver q1a/uandi1.user q1a/uandi1.user.sols 1> q1a/uandi1.user.out 2> q1a/uandi1.user.err 

33:
	mkdir q1a/uandi10.user.sols;./gjsolver q1a/uandi10.user q1a/uandi10.user.sols 1> q1a/uandi10.user.out 2> q1a/uandi10.user.err 

34:
	mkdir q1a/uandi11.user.sols;./gjsolver q1a/uandi11.user q1a/uandi11.user.sols 1> q1a/uandi11.user.out 2> q1a/uandi11.user.err 

35:
	mkdir q1a/uandi12.user.sols;./gjsolver q1a/uandi12.user q1a/uandi12.user.sols 1> q1a/uandi12.user.out 2> q1a/uandi12.user.err 

36:
	mkdir q1a/uandi13.user.sols;./gjsolver q1a/uandi13.user q1a/uandi13.user.sols 1> q1a/uandi13.user.out 2> q1a/uandi13.user.err 

37:
	mkdir q1a/uandi14.user.sols;./gjsolver q1a/uandi14.user q1a/uandi14.user.sols 1> q1a/uandi14.user.out 2> q1a/uandi14.user.err 

38:
	mkdir q1a/uandi15.user.sols;./gjsolver q1a/uandi15.user q1a/uandi15.user.sols 1> q1a/uandi15.user.out 2> q1a/uandi15.user.err 

39:
	mkdir q1a/uandi16.user.sols;./gjsolver q1a/uandi16.user q1a/uandi16.user.sols 1> q1a/uandi16.user.out 2> q1a/uandi16.user.err 

40:
	mkdir q1a/uandi17.user.sols;./gjsolver q1a/uandi17.user q1a/uandi17.user.sols 1> q1a/uandi17.user.out 2> q1a/uandi17.user.err 

41:
	mkdir q1a/uandi18.user.sols;./gjsolver q1a/uandi18.user q1a/uandi18.user.sols 1> q1a/uandi18.user.out 2> q1a/uandi18.user.err 

42:
	mkdir q1a/uandi19.user.sols;./gjsolver q1a/uandi19.user q1a/uandi19.user.sols 1> q1a/uandi19.user.out 2> q1a/uandi19.user.err 

43:
	mkdir q1a/uandi2.user.sols;./gjsolver q1a/uandi2.user q1a/uandi2.user.sols 1> q1a/uandi2.user.out 2> q1a/uandi2.user.err 

44:
	mkdir q1a/uandi20.user.sols;./gjsolver q1a/uandi20.user q1a/uandi20.user.sols 1> q1a/uandi20.user.out 2> q1a/uandi20.user.err 

45:
	mkdir q1a/uandi21.user.sols;./gjsolver q1a/uandi21.user q1a/uandi21.user.sols 1> q1a/uandi21.user.out 2> q1a/uandi21.user.err 

46:
	mkdir q1a/uandi22.user.sols;./gjsolver q1a/uandi22.user q1a/uandi22.user.sols 1> q1a/uandi22.user.out 2> q1a/uandi22.user.err 

47:
	mkdir q1a/uandi23.user.sols;./gjsolver q1a/uandi23.user q1a/uandi23.user.sols 1> q1a/uandi23.user.out 2> q1a/uandi23.user.err 

48:
	mkdir q1a/uandi24.user.sols;./gjsolver q1a/uandi24.user q1a/uandi24.user.sols 1> q1a/uandi24.user.out 2> q1a/uandi24.user.err 

49:
	mkdir q1a/uandi25.user.sols;./gjsolver q1a/uandi25.user q1a/uandi25.user.sols 1> q1a/uandi25.user.out 2> q1a/uandi25.user.err 

50:
	mkdir q1a/uandi26.user.sols;./gjsolver q1a/uandi26.user q1a/uandi26.user.sols 1> q1a/uandi26.user.out 2> q1a/uandi26.user.err 

51:
	mkdir q1a/uandi27.user.sols;./gjsolver q1a/uandi27.user q1a/uandi27.user.sols 1> q1a/uandi27.user.out 2> q1a/uandi27.user.err 

52:
	mkdir q1a/uandi28.user.sols;./gjsolver q1a/uandi28.user q1a/uandi28.user.sols 1> q1a/uandi28.user.out 2> q1a/uandi28.user.err 

53:
	mkdir q1a/uandi29.user.sols;./gjsolver q1a/uandi29.user q1a/uandi29.user.sols 1> q1a/uandi29.user.out 2> q1a/uandi29.user.err 

54:
	mkdir q1a/uandi3.user.sols;./gjsolver q1a/uandi3.user q1a/uandi3.user.sols 1> q1a/uandi3.user.out 2> q1a/uandi3.user.err 

55:
	mkdir q1a/uandi30.user.sols;./gjsolver q1a/uandi30.user q1a/uandi30.user.sols 1> q1a/uandi30.user.out 2> q1a/uandi30.user.err 

56:
	mkdir q1a/uandi4.user.sols;./gjsolver q1a/uandi4.user q1a/uandi4.user.sols 1> q1a/uandi4.user.out 2> q1a/uandi4.user.err 

57:
	mkdir q1a/uandi5.user.sols;./gjsolver q1a/uandi5.user q1a/uandi5.user.sols 1> q1a/uandi5.user.out 2> q1a/uandi5.user.err 

58:
	mkdir q1a/uandi6.user.sols;./gjsolver q1a/uandi6.user q1a/uandi6.user.sols 1> q1a/uandi6.user.out 2> q1a/uandi6.user.err 

59:
	mkdir q1a/uandi7.user.sols;./gjsolver q1a/uandi7.user q1a/uandi7.user.sols 1> q1a/uandi7.user.out 2> q1a/uandi7.user.err 

60:
	mkdir q1a/uandi8.user.sols;./gjsolver q1a/uandi8.user q1a/uandi8.user.sols 1> q1a/uandi8.user.out 2> q1a/uandi8.user.err 

61:
	mkdir q1a/uandi9.user.sols;./gjsolver q1a/uandi9.user q1a/uandi9.user.sols 1> q1a/uandi9.user.out 2> q1a/uandi9.user.err 

62:
	mkdir q1b/u0.1.1.user.sols;./gjsolver q1b/u0.1.1.user q1b/u0.1.1.user.sols 1> q1b/u0.1.1.user.out 2> q1b/u0.1.1.user.err 

63:
	mkdir q1b/u0.1.2.user.sols;./gjsolver q1b/u0.1.2.user q1b/u0.1.2.user.sols 1> q1b/u0.1.2.user.out 2> q1b/u0.1.2.user.err 

64:
	mkdir q1b/u0.1.3.user.sols;./gjsolver q1b/u0.1.3.user q1b/u0.1.3.user.sols 1> q1b/u0.1.3.user.out 2> q1b/u0.1.3.user.err 

65:
	mkdir q1b/u0.1.4.user.sols;./gjsolver q1b/u0.1.4.user q1b/u0.1.4.user.sols 1> q1b/u0.1.4.user.out 2> q1b/u0.1.4.user.err 

66:
	mkdir q1b/u0.1.5.user.sols;./gjsolver q1b/u0.1.5.user q1b/u0.1.5.user.sols 1> q1b/u0.1.5.user.out 2> q1b/u0.1.5.user.err 

67:
	mkdir q1b/u0.2.1.user.sols;./gjsolver q1b/u0.2.1.user q1b/u0.2.1.user.sols 1> q1b/u0.2.1.user.out 2> q1b/u0.2.1.user.err 

68:
	mkdir q1b/u0.2.2.user.sols;./gjsolver q1b/u0.2.2.user q1b/u0.2.2.user.sols 1> q1b/u0.2.2.user.out 2> q1b/u0.2.2.user.err 

69:
	mkdir q1b/u0.2.3.user.sols;./gjsolver q1b/u0.2.3.user q1b/u0.2.3.user.sols 1> q1b/u0.2.3.user.out 2> q1b/u0.2.3.user.err 

70:
	mkdir q1b/u0.2.4.user.sols;./gjsolver q1b/u0.2.4.user q1b/u0.2.4.user.sols 1> q1b/u0.2.4.user.out 2> q1b/u0.2.4.user.err 

71:
	mkdir q1b/u0.2.5.user.sols;./gjsolver q1b/u0.2.5.user q1b/u0.2.5.user.sols 1> q1b/u0.2.5.user.out 2> q1b/u0.2.5.user.err 

72:
	mkdir q1b/u0.3.1.user.sols;./gjsolver q1b/u0.3.1.user q1b/u0.3.1.user.sols 1> q1b/u0.3.1.user.out 2> q1b/u0.3.1.user.err 

73:
	mkdir q1b/u0.3.2.user.sols;./gjsolver q1b/u0.3.2.user q1b/u0.3.2.user.sols 1> q1b/u0.3.2.user.out 2> q1b/u0.3.2.user.err 

74:
	mkdir q1b/u0.3.3.user.sols;./gjsolver q1b/u0.3.3.user q1b/u0.3.3.user.sols 1> q1b/u0.3.3.user.out 2> q1b/u0.3.3.user.err 

75:
	mkdir q1b/u0.3.4.user.sols;./gjsolver q1b/u0.3.4.user q1b/u0.3.4.user.sols 1> q1b/u0.3.4.user.out 2> q1b/u0.3.4.user.err 

76:
	mkdir q1b/u0.3.5.user.sols;./gjsolver q1b/u0.3.5.user q1b/u0.3.5.user.sols 1> q1b/u0.3.5.user.out 2> q1b/u0.3.5.user.err 

77:
	mkdir q1b/u0.4.1.user.sols;./gjsolver q1b/u0.4.1.user q1b/u0.4.1.user.sols 1> q1b/u0.4.1.user.out 2> q1b/u0.4.1.user.err 

78:
	mkdir q1b/u0.4.2.user.sols;./gjsolver q1b/u0.4.2.user q1b/u0.4.2.user.sols 1> q1b/u0.4.2.user.out 2> q1b/u0.4.2.user.err 

79:
	mkdir q1b/u0.4.3.user.sols;./gjsolver q1b/u0.4.3.user q1b/u0.4.3.user.sols 1> q1b/u0.4.3.user.out 2> q1b/u0.4.3.user.err 

80:
	mkdir q1b/u0.4.4.user.sols;./gjsolver q1b/u0.4.4.user q1b/u0.4.4.user.sols 1> q1b/u0.4.4.user.out 2> q1b/u0.4.4.user.err 

81:
	mkdir q1b/u0.4.5.user.sols;./gjsolver q1b/u0.4.5.user q1b/u0.4.5.user.sols 1> q1b/u0.4.5.user.out 2> q1b/u0.4.5.user.err 

82:
	mkdir q1b/u0.5.1.user.sols;./gjsolver q1b/u0.5.1.user q1b/u0.5.1.user.sols 1> q1b/u0.5.1.user.out 2> q1b/u0.5.1.user.err 

83:
	mkdir q1b/u0.5.2.user.sols;./gjsolver q1b/u0.5.2.user q1b/u0.5.2.user.sols 1> q1b/u0.5.2.user.out 2> q1b/u0.5.2.user.err 

84:
	mkdir q1b/u0.5.3.user.sols;./gjsolver q1b/u0.5.3.user q1b/u0.5.3.user.sols 1> q1b/u0.5.3.user.out 2> q1b/u0.5.3.user.err 

85:
	mkdir q1b/u0.5.4.user.sols;./gjsolver q1b/u0.5.4.user q1b/u0.5.4.user.sols 1> q1b/u0.5.4.user.out 2> q1b/u0.5.4.user.err 

86:
	mkdir q1b/u0.5.5.user.sols;./gjsolver q1b/u0.5.5.user q1b/u0.5.5.user.sols 1> q1b/u0.5.5.user.out 2> q1b/u0.5.5.user.err 

87:
	mkdir q1b/u0.6.1.user.sols;./gjsolver q1b/u0.6.1.user q1b/u0.6.1.user.sols 1> q1b/u0.6.1.user.out 2> q1b/u0.6.1.user.err 

88:
	mkdir q1b/u0.6.2.user.sols;./gjsolver q1b/u0.6.2.user q1b/u0.6.2.user.sols 1> q1b/u0.6.2.user.out 2> q1b/u0.6.2.user.err 

89:
	mkdir q1b/u0.6.3.user.sols;./gjsolver q1b/u0.6.3.user q1b/u0.6.3.user.sols 1> q1b/u0.6.3.user.out 2> q1b/u0.6.3.user.err 

90:
	mkdir q1b/u0.6.4.user.sols;./gjsolver q1b/u0.6.4.user q1b/u0.6.4.user.sols 1> q1b/u0.6.4.user.out 2> q1b/u0.6.4.user.err 

91:
	mkdir q1b/u0.6.5.user.sols;./gjsolver q1b/u0.6.5.user q1b/u0.6.5.user.sols 1> q1b/u0.6.5.user.out 2> q1b/u0.6.5.user.err 

92:
	mkdir q1b/u0.7.1.user.sols;./gjsolver q1b/u0.7.1.user q1b/u0.7.1.user.sols 1> q1b/u0.7.1.user.out 2> q1b/u0.7.1.user.err 

93:
	mkdir q1b/u0.7.2.user.sols;./gjsolver q1b/u0.7.2.user q1b/u0.7.2.user.sols 1> q1b/u0.7.2.user.out 2> q1b/u0.7.2.user.err 

94:
	mkdir q1b/u0.7.3.user.sols;./gjsolver q1b/u0.7.3.user q1b/u0.7.3.user.sols 1> q1b/u0.7.3.user.out 2> q1b/u0.7.3.user.err 

95:
	mkdir q1b/u0.7.4.user.sols;./gjsolver q1b/u0.7.4.user q1b/u0.7.4.user.sols 1> q1b/u0.7.4.user.out 2> q1b/u0.7.4.user.err 

96:
	mkdir q1b/u0.7.5.user.sols;./gjsolver q1b/u0.7.5.user q1b/u0.7.5.user.sols 1> q1b/u0.7.5.user.out 2> q1b/u0.7.5.user.err 

97:
	mkdir q1b/u0.8.1.user.sols;./gjsolver q1b/u0.8.1.user q1b/u0.8.1.user.sols 1> q1b/u0.8.1.user.out 2> q1b/u0.8.1.user.err 

98:
	mkdir q1b/u0.8.2.user.sols;./gjsolver q1b/u0.8.2.user q1b/u0.8.2.user.sols 1> q1b/u0.8.2.user.out 2> q1b/u0.8.2.user.err 

99:
	mkdir q1b/u0.8.3.user.sols;./gjsolver q1b/u0.8.3.user q1b/u0.8.3.user.sols 1> q1b/u0.8.3.user.out 2> q1b/u0.8.3.user.err 

100:
	mkdir q1b/u0.8.4.user.sols;./gjsolver q1b/u0.8.4.user q1b/u0.8.4.user.sols 1> q1b/u0.8.4.user.out 2> q1b/u0.8.4.user.err 

101:
	mkdir q1b/u0.8.5.user.sols;./gjsolver q1b/u0.8.5.user q1b/u0.8.5.user.sols 1> q1b/u0.8.5.user.out 2> q1b/u0.8.5.user.err 

102:
	mkdir q1b/u0.9.1.user.sols;./gjsolver q1b/u0.9.1.user q1b/u0.9.1.user.sols 1> q1b/u0.9.1.user.out 2> q1b/u0.9.1.user.err 

103:
	mkdir q1b/u0.9.2.user.sols;./gjsolver q1b/u0.9.2.user q1b/u0.9.2.user.sols 1> q1b/u0.9.2.user.out 2> q1b/u0.9.2.user.err 

104:
	mkdir q1b/u0.9.3.user.sols;./gjsolver q1b/u0.9.3.user q1b/u0.9.3.user.sols 1> q1b/u0.9.3.user.out 2> q1b/u0.9.3.user.err 

105:
	mkdir q1b/u0.9.4.user.sols;./gjsolver q1b/u0.9.4.user q1b/u0.9.4.user.sols 1> q1b/u0.9.4.user.out 2> q1b/u0.9.4.user.err 

106:
	mkdir q1b/u0.9.5.user.sols;./gjsolver q1b/u0.9.5.user q1b/u0.9.5.user.sols 1> q1b/u0.9.5.user.out 2> q1b/u0.9.5.user.err 

107:
	mkdir q1c/i0.1.1.user.sols;./gjsolver q1c/i0.1.1.user q1c/i0.1.1.user.sols 1> q1c/i0.1.1.user.out 2> q1c/i0.1.1.user.err 

108:
	mkdir q1c/i0.1.10.user.sols;./gjsolver q1c/i0.1.10.user q1c/i0.1.10.user.sols 1> q1c/i0.1.10.user.out 2> q1c/i0.1.10.user.err 

109:
	mkdir q1c/i0.1.11.user.sols;./gjsolver q1c/i0.1.11.user q1c/i0.1.11.user.sols 1> q1c/i0.1.11.user.out 2> q1c/i0.1.11.user.err 

110:
	mkdir q1c/i0.1.12.user.sols;./gjsolver q1c/i0.1.12.user q1c/i0.1.12.user.sols 1> q1c/i0.1.12.user.out 2> q1c/i0.1.12.user.err 

111:
	mkdir q1c/i0.1.13.user.sols;./gjsolver q1c/i0.1.13.user q1c/i0.1.13.user.sols 1> q1c/i0.1.13.user.out 2> q1c/i0.1.13.user.err 

112:
	mkdir q1c/i0.1.14.user.sols;./gjsolver q1c/i0.1.14.user q1c/i0.1.14.user.sols 1> q1c/i0.1.14.user.out 2> q1c/i0.1.14.user.err 

113:
	mkdir q1c/i0.1.15.user.sols;./gjsolver q1c/i0.1.15.user q1c/i0.1.15.user.sols 1> q1c/i0.1.15.user.out 2> q1c/i0.1.15.user.err 

114:
	mkdir q1c/i0.1.16.user.sols;./gjsolver q1c/i0.1.16.user q1c/i0.1.16.user.sols 1> q1c/i0.1.16.user.out 2> q1c/i0.1.16.user.err 

115:
	mkdir q1c/i0.1.17.user.sols;./gjsolver q1c/i0.1.17.user q1c/i0.1.17.user.sols 1> q1c/i0.1.17.user.out 2> q1c/i0.1.17.user.err 

116:
	mkdir q1c/i0.1.18.user.sols;./gjsolver q1c/i0.1.18.user q1c/i0.1.18.user.sols 1> q1c/i0.1.18.user.out 2> q1c/i0.1.18.user.err 

117:
	mkdir q1c/i0.1.19.user.sols;./gjsolver q1c/i0.1.19.user q1c/i0.1.19.user.sols 1> q1c/i0.1.19.user.out 2> q1c/i0.1.19.user.err 

118:
	mkdir q1c/i0.1.2.user.sols;./gjsolver q1c/i0.1.2.user q1c/i0.1.2.user.sols 1> q1c/i0.1.2.user.out 2> q1c/i0.1.2.user.err 

119:
	mkdir q1c/i0.1.20.user.sols;./gjsolver q1c/i0.1.20.user q1c/i0.1.20.user.sols 1> q1c/i0.1.20.user.out 2> q1c/i0.1.20.user.err 

120:
	mkdir q1c/i0.1.21.user.sols;./gjsolver q1c/i0.1.21.user q1c/i0.1.21.user.sols 1> q1c/i0.1.21.user.out 2> q1c/i0.1.21.user.err 

121:
	mkdir q1c/i0.1.22.user.sols;./gjsolver q1c/i0.1.22.user q1c/i0.1.22.user.sols 1> q1c/i0.1.22.user.out 2> q1c/i0.1.22.user.err 

122:
	mkdir q1c/i0.1.23.user.sols;./gjsolver q1c/i0.1.23.user q1c/i0.1.23.user.sols 1> q1c/i0.1.23.user.out 2> q1c/i0.1.23.user.err 

123:
	mkdir q1c/i0.1.24.user.sols;./gjsolver q1c/i0.1.24.user q1c/i0.1.24.user.sols 1> q1c/i0.1.24.user.out 2> q1c/i0.1.24.user.err 

124:
	mkdir q1c/i0.1.25.user.sols;./gjsolver q1c/i0.1.25.user q1c/i0.1.25.user.sols 1> q1c/i0.1.25.user.out 2> q1c/i0.1.25.user.err 

125:
	mkdir q1c/i0.1.26.user.sols;./gjsolver q1c/i0.1.26.user q1c/i0.1.26.user.sols 1> q1c/i0.1.26.user.out 2> q1c/i0.1.26.user.err 

126:
	mkdir q1c/i0.1.27.user.sols;./gjsolver q1c/i0.1.27.user q1c/i0.1.27.user.sols 1> q1c/i0.1.27.user.out 2> q1c/i0.1.27.user.err 

127:
	mkdir q1c/i0.1.28.user.sols;./gjsolver q1c/i0.1.28.user q1c/i0.1.28.user.sols 1> q1c/i0.1.28.user.out 2> q1c/i0.1.28.user.err 

128:
	mkdir q1c/i0.1.29.user.sols;./gjsolver q1c/i0.1.29.user q1c/i0.1.29.user.sols 1> q1c/i0.1.29.user.out 2> q1c/i0.1.29.user.err 

129:
	mkdir q1c/i0.1.3.user.sols;./gjsolver q1c/i0.1.3.user q1c/i0.1.3.user.sols 1> q1c/i0.1.3.user.out 2> q1c/i0.1.3.user.err 

130:
	mkdir q1c/i0.1.30.user.sols;./gjsolver q1c/i0.1.30.user q1c/i0.1.30.user.sols 1> q1c/i0.1.30.user.out 2> q1c/i0.1.30.user.err 

131:
	mkdir q1c/i0.1.4.user.sols;./gjsolver q1c/i0.1.4.user q1c/i0.1.4.user.sols 1> q1c/i0.1.4.user.out 2> q1c/i0.1.4.user.err 

132:
	mkdir q1c/i0.1.5.user.sols;./gjsolver q1c/i0.1.5.user q1c/i0.1.5.user.sols 1> q1c/i0.1.5.user.out 2> q1c/i0.1.5.user.err 

133:
	mkdir q1c/i0.1.6.user.sols;./gjsolver q1c/i0.1.6.user q1c/i0.1.6.user.sols 1> q1c/i0.1.6.user.out 2> q1c/i0.1.6.user.err 

134:
	mkdir q1c/i0.1.7.user.sols;./gjsolver q1c/i0.1.7.user q1c/i0.1.7.user.sols 1> q1c/i0.1.7.user.out 2> q1c/i0.1.7.user.err 

135:
	mkdir q1c/i0.1.8.user.sols;./gjsolver q1c/i0.1.8.user q1c/i0.1.8.user.sols 1> q1c/i0.1.8.user.out 2> q1c/i0.1.8.user.err 

136:
	mkdir q1c/i0.1.9.user.sols;./gjsolver q1c/i0.1.9.user q1c/i0.1.9.user.sols 1> q1c/i0.1.9.user.out 2> q1c/i0.1.9.user.err 

137:
	mkdir q1c/i0.2.1.user.sols;./gjsolver q1c/i0.2.1.user q1c/i0.2.1.user.sols 1> q1c/i0.2.1.user.out 2> q1c/i0.2.1.user.err 

138:
	mkdir q1c/i0.2.10.user.sols;./gjsolver q1c/i0.2.10.user q1c/i0.2.10.user.sols 1> q1c/i0.2.10.user.out 2> q1c/i0.2.10.user.err 

139:
	mkdir q1c/i0.2.11.user.sols;./gjsolver q1c/i0.2.11.user q1c/i0.2.11.user.sols 1> q1c/i0.2.11.user.out 2> q1c/i0.2.11.user.err 

140:
	mkdir q1c/i0.2.12.user.sols;./gjsolver q1c/i0.2.12.user q1c/i0.2.12.user.sols 1> q1c/i0.2.12.user.out 2> q1c/i0.2.12.user.err 

141:
	mkdir q1c/i0.2.13.user.sols;./gjsolver q1c/i0.2.13.user q1c/i0.2.13.user.sols 1> q1c/i0.2.13.user.out 2> q1c/i0.2.13.user.err 

142:
	mkdir q1c/i0.2.14.user.sols;./gjsolver q1c/i0.2.14.user q1c/i0.2.14.user.sols 1> q1c/i0.2.14.user.out 2> q1c/i0.2.14.user.err 

143:
	mkdir q1c/i0.2.15.user.sols;./gjsolver q1c/i0.2.15.user q1c/i0.2.15.user.sols 1> q1c/i0.2.15.user.out 2> q1c/i0.2.15.user.err 

144:
	mkdir q1c/i0.2.16.user.sols;./gjsolver q1c/i0.2.16.user q1c/i0.2.16.user.sols 1> q1c/i0.2.16.user.out 2> q1c/i0.2.16.user.err 

145:
	mkdir q1c/i0.2.17.user.sols;./gjsolver q1c/i0.2.17.user q1c/i0.2.17.user.sols 1> q1c/i0.2.17.user.out 2> q1c/i0.2.17.user.err 

146:
	mkdir q1c/i0.2.18.user.sols;./gjsolver q1c/i0.2.18.user q1c/i0.2.18.user.sols 1> q1c/i0.2.18.user.out 2> q1c/i0.2.18.user.err 

147:
	mkdir q1c/i0.2.19.user.sols;./gjsolver q1c/i0.2.19.user q1c/i0.2.19.user.sols 1> q1c/i0.2.19.user.out 2> q1c/i0.2.19.user.err 

148:
	mkdir q1c/i0.2.2.user.sols;./gjsolver q1c/i0.2.2.user q1c/i0.2.2.user.sols 1> q1c/i0.2.2.user.out 2> q1c/i0.2.2.user.err 

149:
	mkdir q1c/i0.2.20.user.sols;./gjsolver q1c/i0.2.20.user q1c/i0.2.20.user.sols 1> q1c/i0.2.20.user.out 2> q1c/i0.2.20.user.err 

150:
	mkdir q1c/i0.2.21.user.sols;./gjsolver q1c/i0.2.21.user q1c/i0.2.21.user.sols 1> q1c/i0.2.21.user.out 2> q1c/i0.2.21.user.err 

151:
	mkdir q1c/i0.2.22.user.sols;./gjsolver q1c/i0.2.22.user q1c/i0.2.22.user.sols 1> q1c/i0.2.22.user.out 2> q1c/i0.2.22.user.err 

152:
	mkdir q1c/i0.2.23.user.sols;./gjsolver q1c/i0.2.23.user q1c/i0.2.23.user.sols 1> q1c/i0.2.23.user.out 2> q1c/i0.2.23.user.err 

153:
	mkdir q1c/i0.2.24.user.sols;./gjsolver q1c/i0.2.24.user q1c/i0.2.24.user.sols 1> q1c/i0.2.24.user.out 2> q1c/i0.2.24.user.err 

154:
	mkdir q1c/i0.2.25.user.sols;./gjsolver q1c/i0.2.25.user q1c/i0.2.25.user.sols 1> q1c/i0.2.25.user.out 2> q1c/i0.2.25.user.err 

155:
	mkdir q1c/i0.2.26.user.sols;./gjsolver q1c/i0.2.26.user q1c/i0.2.26.user.sols 1> q1c/i0.2.26.user.out 2> q1c/i0.2.26.user.err 

156:
	mkdir q1c/i0.2.27.user.sols;./gjsolver q1c/i0.2.27.user q1c/i0.2.27.user.sols 1> q1c/i0.2.27.user.out 2> q1c/i0.2.27.user.err 

157:
	mkdir q1c/i0.2.28.user.sols;./gjsolver q1c/i0.2.28.user q1c/i0.2.28.user.sols 1> q1c/i0.2.28.user.out 2> q1c/i0.2.28.user.err 

158:
	mkdir q1c/i0.2.29.user.sols;./gjsolver q1c/i0.2.29.user q1c/i0.2.29.user.sols 1> q1c/i0.2.29.user.out 2> q1c/i0.2.29.user.err 

159:
	mkdir q1c/i0.2.3.user.sols;./gjsolver q1c/i0.2.3.user q1c/i0.2.3.user.sols 1> q1c/i0.2.3.user.out 2> q1c/i0.2.3.user.err 

160:
	mkdir q1c/i0.2.30.user.sols;./gjsolver q1c/i0.2.30.user q1c/i0.2.30.user.sols 1> q1c/i0.2.30.user.out 2> q1c/i0.2.30.user.err 

161:
	mkdir q1c/i0.2.4.user.sols;./gjsolver q1c/i0.2.4.user q1c/i0.2.4.user.sols 1> q1c/i0.2.4.user.out 2> q1c/i0.2.4.user.err 

162:
	mkdir q1c/i0.2.5.user.sols;./gjsolver q1c/i0.2.5.user q1c/i0.2.5.user.sols 1> q1c/i0.2.5.user.out 2> q1c/i0.2.5.user.err 

163:
	mkdir q1c/i0.2.6.user.sols;./gjsolver q1c/i0.2.6.user q1c/i0.2.6.user.sols 1> q1c/i0.2.6.user.out 2> q1c/i0.2.6.user.err 

164:
	mkdir q1c/i0.2.7.user.sols;./gjsolver q1c/i0.2.7.user q1c/i0.2.7.user.sols 1> q1c/i0.2.7.user.out 2> q1c/i0.2.7.user.err 

165:
	mkdir q1c/i0.2.8.user.sols;./gjsolver q1c/i0.2.8.user q1c/i0.2.8.user.sols 1> q1c/i0.2.8.user.out 2> q1c/i0.2.8.user.err 

166:
	mkdir q1c/i0.2.9.user.sols;./gjsolver q1c/i0.2.9.user q1c/i0.2.9.user.sols 1> q1c/i0.2.9.user.out 2> q1c/i0.2.9.user.err 

167:
	mkdir q1c/i0.3.1.user.sols;./gjsolver q1c/i0.3.1.user q1c/i0.3.1.user.sols 1> q1c/i0.3.1.user.out 2> q1c/i0.3.1.user.err 

168:
	mkdir q1c/i0.3.10.user.sols;./gjsolver q1c/i0.3.10.user q1c/i0.3.10.user.sols 1> q1c/i0.3.10.user.out 2> q1c/i0.3.10.user.err 

169:
	mkdir q1c/i0.3.11.user.sols;./gjsolver q1c/i0.3.11.user q1c/i0.3.11.user.sols 1> q1c/i0.3.11.user.out 2> q1c/i0.3.11.user.err 

170:
	mkdir q1c/i0.3.12.user.sols;./gjsolver q1c/i0.3.12.user q1c/i0.3.12.user.sols 1> q1c/i0.3.12.user.out 2> q1c/i0.3.12.user.err 

171:
	mkdir q1c/i0.3.13.user.sols;./gjsolver q1c/i0.3.13.user q1c/i0.3.13.user.sols 1> q1c/i0.3.13.user.out 2> q1c/i0.3.13.user.err 

172:
	mkdir q1c/i0.3.14.user.sols;./gjsolver q1c/i0.3.14.user q1c/i0.3.14.user.sols 1> q1c/i0.3.14.user.out 2> q1c/i0.3.14.user.err 

173:
	mkdir q1c/i0.3.15.user.sols;./gjsolver q1c/i0.3.15.user q1c/i0.3.15.user.sols 1> q1c/i0.3.15.user.out 2> q1c/i0.3.15.user.err 

174:
	mkdir q1c/i0.3.16.user.sols;./gjsolver q1c/i0.3.16.user q1c/i0.3.16.user.sols 1> q1c/i0.3.16.user.out 2> q1c/i0.3.16.user.err 

175:
	mkdir q1c/i0.3.17.user.sols;./gjsolver q1c/i0.3.17.user q1c/i0.3.17.user.sols 1> q1c/i0.3.17.user.out 2> q1c/i0.3.17.user.err 

176:
	mkdir q1c/i0.3.18.user.sols;./gjsolver q1c/i0.3.18.user q1c/i0.3.18.user.sols 1> q1c/i0.3.18.user.out 2> q1c/i0.3.18.user.err 

177:
	mkdir q1c/i0.3.19.user.sols;./gjsolver q1c/i0.3.19.user q1c/i0.3.19.user.sols 1> q1c/i0.3.19.user.out 2> q1c/i0.3.19.user.err 

178:
	mkdir q1c/i0.3.2.user.sols;./gjsolver q1c/i0.3.2.user q1c/i0.3.2.user.sols 1> q1c/i0.3.2.user.out 2> q1c/i0.3.2.user.err 

179:
	mkdir q1c/i0.3.20.user.sols;./gjsolver q1c/i0.3.20.user q1c/i0.3.20.user.sols 1> q1c/i0.3.20.user.out 2> q1c/i0.3.20.user.err 

180:
	mkdir q1c/i0.3.21.user.sols;./gjsolver q1c/i0.3.21.user q1c/i0.3.21.user.sols 1> q1c/i0.3.21.user.out 2> q1c/i0.3.21.user.err 

181:
	mkdir q1c/i0.3.22.user.sols;./gjsolver q1c/i0.3.22.user q1c/i0.3.22.user.sols 1> q1c/i0.3.22.user.out 2> q1c/i0.3.22.user.err 

182:
	mkdir q1c/i0.3.23.user.sols;./gjsolver q1c/i0.3.23.user q1c/i0.3.23.user.sols 1> q1c/i0.3.23.user.out 2> q1c/i0.3.23.user.err 

183:
	mkdir q1c/i0.3.24.user.sols;./gjsolver q1c/i0.3.24.user q1c/i0.3.24.user.sols 1> q1c/i0.3.24.user.out 2> q1c/i0.3.24.user.err 

184:
	mkdir q1c/i0.3.25.user.sols;./gjsolver q1c/i0.3.25.user q1c/i0.3.25.user.sols 1> q1c/i0.3.25.user.out 2> q1c/i0.3.25.user.err 

185:
	mkdir q1c/i0.3.26.user.sols;./gjsolver q1c/i0.3.26.user q1c/i0.3.26.user.sols 1> q1c/i0.3.26.user.out 2> q1c/i0.3.26.user.err 

186:
	mkdir q1c/i0.3.27.user.sols;./gjsolver q1c/i0.3.27.user q1c/i0.3.27.user.sols 1> q1c/i0.3.27.user.out 2> q1c/i0.3.27.user.err 

187:
	mkdir q1c/i0.3.28.user.sols;./gjsolver q1c/i0.3.28.user q1c/i0.3.28.user.sols 1> q1c/i0.3.28.user.out 2> q1c/i0.3.28.user.err 

188:
	mkdir q1c/i0.3.29.user.sols;./gjsolver q1c/i0.3.29.user q1c/i0.3.29.user.sols 1> q1c/i0.3.29.user.out 2> q1c/i0.3.29.user.err 

189:
	mkdir q1c/i0.3.3.user.sols;./gjsolver q1c/i0.3.3.user q1c/i0.3.3.user.sols 1> q1c/i0.3.3.user.out 2> q1c/i0.3.3.user.err 

190:
	mkdir q1c/i0.3.30.user.sols;./gjsolver q1c/i0.3.30.user q1c/i0.3.30.user.sols 1> q1c/i0.3.30.user.out 2> q1c/i0.3.30.user.err 

191:
	mkdir q1c/i0.3.4.user.sols;./gjsolver q1c/i0.3.4.user q1c/i0.3.4.user.sols 1> q1c/i0.3.4.user.out 2> q1c/i0.3.4.user.err 

192:
	mkdir q1c/i0.3.5.user.sols;./gjsolver q1c/i0.3.5.user q1c/i0.3.5.user.sols 1> q1c/i0.3.5.user.out 2> q1c/i0.3.5.user.err 

193:
	mkdir q1c/i0.3.6.user.sols;./gjsolver q1c/i0.3.6.user q1c/i0.3.6.user.sols 1> q1c/i0.3.6.user.out 2> q1c/i0.3.6.user.err 

194:
	mkdir q1c/i0.3.7.user.sols;./gjsolver q1c/i0.3.7.user q1c/i0.3.7.user.sols 1> q1c/i0.3.7.user.out 2> q1c/i0.3.7.user.err 

195:
	mkdir q1c/i0.3.8.user.sols;./gjsolver q1c/i0.3.8.user q1c/i0.3.8.user.sols 1> q1c/i0.3.8.user.out 2> q1c/i0.3.8.user.err 

196:
	mkdir q1c/i0.3.9.user.sols;./gjsolver q1c/i0.3.9.user q1c/i0.3.9.user.sols 1> q1c/i0.3.9.user.out 2> q1c/i0.3.9.user.err 

197:
	mkdir q1c/i0.4.1.user.sols;./gjsolver q1c/i0.4.1.user q1c/i0.4.1.user.sols 1> q1c/i0.4.1.user.out 2> q1c/i0.4.1.user.err 

198:
	mkdir q1c/i0.4.10.user.sols;./gjsolver q1c/i0.4.10.user q1c/i0.4.10.user.sols 1> q1c/i0.4.10.user.out 2> q1c/i0.4.10.user.err 

199:
	mkdir q1c/i0.4.11.user.sols;./gjsolver q1c/i0.4.11.user q1c/i0.4.11.user.sols 1> q1c/i0.4.11.user.out 2> q1c/i0.4.11.user.err 

200:
	mkdir q1c/i0.4.12.user.sols;./gjsolver q1c/i0.4.12.user q1c/i0.4.12.user.sols 1> q1c/i0.4.12.user.out 2> q1c/i0.4.12.user.err 

201:
	mkdir q1c/i0.4.13.user.sols;./gjsolver q1c/i0.4.13.user q1c/i0.4.13.user.sols 1> q1c/i0.4.13.user.out 2> q1c/i0.4.13.user.err 

202:
	mkdir q1c/i0.4.14.user.sols;./gjsolver q1c/i0.4.14.user q1c/i0.4.14.user.sols 1> q1c/i0.4.14.user.out 2> q1c/i0.4.14.user.err 

203:
	mkdir q1c/i0.4.15.user.sols;./gjsolver q1c/i0.4.15.user q1c/i0.4.15.user.sols 1> q1c/i0.4.15.user.out 2> q1c/i0.4.15.user.err 

204:
	mkdir q1c/i0.4.16.user.sols;./gjsolver q1c/i0.4.16.user q1c/i0.4.16.user.sols 1> q1c/i0.4.16.user.out 2> q1c/i0.4.16.user.err 

205:
	mkdir q1c/i0.4.17.user.sols;./gjsolver q1c/i0.4.17.user q1c/i0.4.17.user.sols 1> q1c/i0.4.17.user.out 2> q1c/i0.4.17.user.err 

206:
	mkdir q1c/i0.4.18.user.sols;./gjsolver q1c/i0.4.18.user q1c/i0.4.18.user.sols 1> q1c/i0.4.18.user.out 2> q1c/i0.4.18.user.err 

207:
	mkdir q1c/i0.4.19.user.sols;./gjsolver q1c/i0.4.19.user q1c/i0.4.19.user.sols 1> q1c/i0.4.19.user.out 2> q1c/i0.4.19.user.err 

208:
	mkdir q1c/i0.4.2.user.sols;./gjsolver q1c/i0.4.2.user q1c/i0.4.2.user.sols 1> q1c/i0.4.2.user.out 2> q1c/i0.4.2.user.err 

209:
	mkdir q1c/i0.4.20.user.sols;./gjsolver q1c/i0.4.20.user q1c/i0.4.20.user.sols 1> q1c/i0.4.20.user.out 2> q1c/i0.4.20.user.err 

210:
	mkdir q1c/i0.4.21.user.sols;./gjsolver q1c/i0.4.21.user q1c/i0.4.21.user.sols 1> q1c/i0.4.21.user.out 2> q1c/i0.4.21.user.err 

211:
	mkdir q1c/i0.4.22.user.sols;./gjsolver q1c/i0.4.22.user q1c/i0.4.22.user.sols 1> q1c/i0.4.22.user.out 2> q1c/i0.4.22.user.err 

212:
	mkdir q1c/i0.4.23.user.sols;./gjsolver q1c/i0.4.23.user q1c/i0.4.23.user.sols 1> q1c/i0.4.23.user.out 2> q1c/i0.4.23.user.err 

213:
	mkdir q1c/i0.4.24.user.sols;./gjsolver q1c/i0.4.24.user q1c/i0.4.24.user.sols 1> q1c/i0.4.24.user.out 2> q1c/i0.4.24.user.err 

214:
	mkdir q1c/i0.4.25.user.sols;./gjsolver q1c/i0.4.25.user q1c/i0.4.25.user.sols 1> q1c/i0.4.25.user.out 2> q1c/i0.4.25.user.err 

215:
	mkdir q1c/i0.4.26.user.sols;./gjsolver q1c/i0.4.26.user q1c/i0.4.26.user.sols 1> q1c/i0.4.26.user.out 2> q1c/i0.4.26.user.err 

216:
	mkdir q1c/i0.4.27.user.sols;./gjsolver q1c/i0.4.27.user q1c/i0.4.27.user.sols 1> q1c/i0.4.27.user.out 2> q1c/i0.4.27.user.err 

217:
	mkdir q1c/i0.4.28.user.sols;./gjsolver q1c/i0.4.28.user q1c/i0.4.28.user.sols 1> q1c/i0.4.28.user.out 2> q1c/i0.4.28.user.err 

218:
	mkdir q1c/i0.4.29.user.sols;./gjsolver q1c/i0.4.29.user q1c/i0.4.29.user.sols 1> q1c/i0.4.29.user.out 2> q1c/i0.4.29.user.err 

219:
	mkdir q1c/i0.4.3.user.sols;./gjsolver q1c/i0.4.3.user q1c/i0.4.3.user.sols 1> q1c/i0.4.3.user.out 2> q1c/i0.4.3.user.err 

220:
	mkdir q1c/i0.4.30.user.sols;./gjsolver q1c/i0.4.30.user q1c/i0.4.30.user.sols 1> q1c/i0.4.30.user.out 2> q1c/i0.4.30.user.err 

221:
	mkdir q1c/i0.4.4.user.sols;./gjsolver q1c/i0.4.4.user q1c/i0.4.4.user.sols 1> q1c/i0.4.4.user.out 2> q1c/i0.4.4.user.err 

222:
	mkdir q1c/i0.4.5.user.sols;./gjsolver q1c/i0.4.5.user q1c/i0.4.5.user.sols 1> q1c/i0.4.5.user.out 2> q1c/i0.4.5.user.err 

223:
	mkdir q1c/i0.4.6.user.sols;./gjsolver q1c/i0.4.6.user q1c/i0.4.6.user.sols 1> q1c/i0.4.6.user.out 2> q1c/i0.4.6.user.err 

224:
	mkdir q1c/i0.4.7.user.sols;./gjsolver q1c/i0.4.7.user q1c/i0.4.7.user.sols 1> q1c/i0.4.7.user.out 2> q1c/i0.4.7.user.err 

225:
	mkdir q1c/i0.4.8.user.sols;./gjsolver q1c/i0.4.8.user q1c/i0.4.8.user.sols 1> q1c/i0.4.8.user.out 2> q1c/i0.4.8.user.err 

226:
	mkdir q1c/i0.4.9.user.sols;./gjsolver q1c/i0.4.9.user q1c/i0.4.9.user.sols 1> q1c/i0.4.9.user.out 2> q1c/i0.4.9.user.err 

227:
	mkdir q1c/i0.5.1.user.sols;./gjsolver q1c/i0.5.1.user q1c/i0.5.1.user.sols 1> q1c/i0.5.1.user.out 2> q1c/i0.5.1.user.err 

228:
	mkdir q1c/i0.5.10.user.sols;./gjsolver q1c/i0.5.10.user q1c/i0.5.10.user.sols 1> q1c/i0.5.10.user.out 2> q1c/i0.5.10.user.err 

229:
	mkdir q1c/i0.5.11.user.sols;./gjsolver q1c/i0.5.11.user q1c/i0.5.11.user.sols 1> q1c/i0.5.11.user.out 2> q1c/i0.5.11.user.err 

230:
	mkdir q1c/i0.5.12.user.sols;./gjsolver q1c/i0.5.12.user q1c/i0.5.12.user.sols 1> q1c/i0.5.12.user.out 2> q1c/i0.5.12.user.err 

231:
	mkdir q1c/i0.5.13.user.sols;./gjsolver q1c/i0.5.13.user q1c/i0.5.13.user.sols 1> q1c/i0.5.13.user.out 2> q1c/i0.5.13.user.err 

232:
	mkdir q1c/i0.5.14.user.sols;./gjsolver q1c/i0.5.14.user q1c/i0.5.14.user.sols 1> q1c/i0.5.14.user.out 2> q1c/i0.5.14.user.err 

233:
	mkdir q1c/i0.5.15.user.sols;./gjsolver q1c/i0.5.15.user q1c/i0.5.15.user.sols 1> q1c/i0.5.15.user.out 2> q1c/i0.5.15.user.err 

234:
	mkdir q1c/i0.5.16.user.sols;./gjsolver q1c/i0.5.16.user q1c/i0.5.16.user.sols 1> q1c/i0.5.16.user.out 2> q1c/i0.5.16.user.err 

235:
	mkdir q1c/i0.5.17.user.sols;./gjsolver q1c/i0.5.17.user q1c/i0.5.17.user.sols 1> q1c/i0.5.17.user.out 2> q1c/i0.5.17.user.err 

236:
	mkdir q1c/i0.5.18.user.sols;./gjsolver q1c/i0.5.18.user q1c/i0.5.18.user.sols 1> q1c/i0.5.18.user.out 2> q1c/i0.5.18.user.err 

237:
	mkdir q1c/i0.5.19.user.sols;./gjsolver q1c/i0.5.19.user q1c/i0.5.19.user.sols 1> q1c/i0.5.19.user.out 2> q1c/i0.5.19.user.err 

238:
	mkdir q1c/i0.5.2.user.sols;./gjsolver q1c/i0.5.2.user q1c/i0.5.2.user.sols 1> q1c/i0.5.2.user.out 2> q1c/i0.5.2.user.err 

239:
	mkdir q1c/i0.5.20.user.sols;./gjsolver q1c/i0.5.20.user q1c/i0.5.20.user.sols 1> q1c/i0.5.20.user.out 2> q1c/i0.5.20.user.err 

240:
	mkdir q1c/i0.5.21.user.sols;./gjsolver q1c/i0.5.21.user q1c/i0.5.21.user.sols 1> q1c/i0.5.21.user.out 2> q1c/i0.5.21.user.err 

241:
	mkdir q1c/i0.5.22.user.sols;./gjsolver q1c/i0.5.22.user q1c/i0.5.22.user.sols 1> q1c/i0.5.22.user.out 2> q1c/i0.5.22.user.err 

242:
	mkdir q1c/i0.5.23.user.sols;./gjsolver q1c/i0.5.23.user q1c/i0.5.23.user.sols 1> q1c/i0.5.23.user.out 2> q1c/i0.5.23.user.err 

243:
	mkdir q1c/i0.5.24.user.sols;./gjsolver q1c/i0.5.24.user q1c/i0.5.24.user.sols 1> q1c/i0.5.24.user.out 2> q1c/i0.5.24.user.err 

244:
	mkdir q1c/i0.5.25.user.sols;./gjsolver q1c/i0.5.25.user q1c/i0.5.25.user.sols 1> q1c/i0.5.25.user.out 2> q1c/i0.5.25.user.err 

245:
	mkdir q1c/i0.5.26.user.sols;./gjsolver q1c/i0.5.26.user q1c/i0.5.26.user.sols 1> q1c/i0.5.26.user.out 2> q1c/i0.5.26.user.err 

246:
	mkdir q1c/i0.5.27.user.sols;./gjsolver q1c/i0.5.27.user q1c/i0.5.27.user.sols 1> q1c/i0.5.27.user.out 2> q1c/i0.5.27.user.err 

247:
	mkdir q1c/i0.5.28.user.sols;./gjsolver q1c/i0.5.28.user q1c/i0.5.28.user.sols 1> q1c/i0.5.28.user.out 2> q1c/i0.5.28.user.err 

248:
	mkdir q1c/i0.5.29.user.sols;./gjsolver q1c/i0.5.29.user q1c/i0.5.29.user.sols 1> q1c/i0.5.29.user.out 2> q1c/i0.5.29.user.err 

249:
	mkdir q1c/i0.5.3.user.sols;./gjsolver q1c/i0.5.3.user q1c/i0.5.3.user.sols 1> q1c/i0.5.3.user.out 2> q1c/i0.5.3.user.err 

250:
	mkdir q1c/i0.5.30.user.sols;./gjsolver q1c/i0.5.30.user q1c/i0.5.30.user.sols 1> q1c/i0.5.30.user.out 2> q1c/i0.5.30.user.err 

251:
	mkdir q1c/i0.5.4.user.sols;./gjsolver q1c/i0.5.4.user q1c/i0.5.4.user.sols 1> q1c/i0.5.4.user.out 2> q1c/i0.5.4.user.err 

252:
	mkdir q1c/i0.5.5.user.sols;./gjsolver q1c/i0.5.5.user q1c/i0.5.5.user.sols 1> q1c/i0.5.5.user.out 2> q1c/i0.5.5.user.err 

253:
	mkdir q1c/i0.5.6.user.sols;./gjsolver q1c/i0.5.6.user q1c/i0.5.6.user.sols 1> q1c/i0.5.6.user.out 2> q1c/i0.5.6.user.err 

254:
	mkdir q1c/i0.5.7.user.sols;./gjsolver q1c/i0.5.7.user q1c/i0.5.7.user.sols 1> q1c/i0.5.7.user.out 2> q1c/i0.5.7.user.err 

255:
	mkdir q1c/i0.5.8.user.sols;./gjsolver q1c/i0.5.8.user q1c/i0.5.8.user.sols 1> q1c/i0.5.8.user.out 2> q1c/i0.5.8.user.err 

256:
	mkdir q1c/i0.5.9.user.sols;./gjsolver q1c/i0.5.9.user q1c/i0.5.9.user.sols 1> q1c/i0.5.9.user.out 2> q1c/i0.5.9.user.err 

257:
	mkdir q1c/i0.6.1.user.sols;./gjsolver q1c/i0.6.1.user q1c/i0.6.1.user.sols 1> q1c/i0.6.1.user.out 2> q1c/i0.6.1.user.err 

258:
	mkdir q1c/i0.6.10.user.sols;./gjsolver q1c/i0.6.10.user q1c/i0.6.10.user.sols 1> q1c/i0.6.10.user.out 2> q1c/i0.6.10.user.err 

259:
	mkdir q1c/i0.6.11.user.sols;./gjsolver q1c/i0.6.11.user q1c/i0.6.11.user.sols 1> q1c/i0.6.11.user.out 2> q1c/i0.6.11.user.err 

260:
	mkdir q1c/i0.6.12.user.sols;./gjsolver q1c/i0.6.12.user q1c/i0.6.12.user.sols 1> q1c/i0.6.12.user.out 2> q1c/i0.6.12.user.err 

261:
	mkdir q1c/i0.6.13.user.sols;./gjsolver q1c/i0.6.13.user q1c/i0.6.13.user.sols 1> q1c/i0.6.13.user.out 2> q1c/i0.6.13.user.err 

262:
	mkdir q1c/i0.6.14.user.sols;./gjsolver q1c/i0.6.14.user q1c/i0.6.14.user.sols 1> q1c/i0.6.14.user.out 2> q1c/i0.6.14.user.err 

263:
	mkdir q1c/i0.6.15.user.sols;./gjsolver q1c/i0.6.15.user q1c/i0.6.15.user.sols 1> q1c/i0.6.15.user.out 2> q1c/i0.6.15.user.err 

264:
	mkdir q1c/i0.6.16.user.sols;./gjsolver q1c/i0.6.16.user q1c/i0.6.16.user.sols 1> q1c/i0.6.16.user.out 2> q1c/i0.6.16.user.err 

265:
	mkdir q1c/i0.6.17.user.sols;./gjsolver q1c/i0.6.17.user q1c/i0.6.17.user.sols 1> q1c/i0.6.17.user.out 2> q1c/i0.6.17.user.err 

266:
	mkdir q1c/i0.6.18.user.sols;./gjsolver q1c/i0.6.18.user q1c/i0.6.18.user.sols 1> q1c/i0.6.18.user.out 2> q1c/i0.6.18.user.err 

267:
	mkdir q1c/i0.6.19.user.sols;./gjsolver q1c/i0.6.19.user q1c/i0.6.19.user.sols 1> q1c/i0.6.19.user.out 2> q1c/i0.6.19.user.err 

268:
	mkdir q1c/i0.6.2.user.sols;./gjsolver q1c/i0.6.2.user q1c/i0.6.2.user.sols 1> q1c/i0.6.2.user.out 2> q1c/i0.6.2.user.err 

269:
	mkdir q1c/i0.6.20.user.sols;./gjsolver q1c/i0.6.20.user q1c/i0.6.20.user.sols 1> q1c/i0.6.20.user.out 2> q1c/i0.6.20.user.err 

270:
	mkdir q1c/i0.6.21.user.sols;./gjsolver q1c/i0.6.21.user q1c/i0.6.21.user.sols 1> q1c/i0.6.21.user.out 2> q1c/i0.6.21.user.err 

271:
	mkdir q1c/i0.6.22.user.sols;./gjsolver q1c/i0.6.22.user q1c/i0.6.22.user.sols 1> q1c/i0.6.22.user.out 2> q1c/i0.6.22.user.err 

272:
	mkdir q1c/i0.6.23.user.sols;./gjsolver q1c/i0.6.23.user q1c/i0.6.23.user.sols 1> q1c/i0.6.23.user.out 2> q1c/i0.6.23.user.err 

273:
	mkdir q1c/i0.6.24.user.sols;./gjsolver q1c/i0.6.24.user q1c/i0.6.24.user.sols 1> q1c/i0.6.24.user.out 2> q1c/i0.6.24.user.err 

274:
	mkdir q1c/i0.6.25.user.sols;./gjsolver q1c/i0.6.25.user q1c/i0.6.25.user.sols 1> q1c/i0.6.25.user.out 2> q1c/i0.6.25.user.err 

275:
	mkdir q1c/i0.6.26.user.sols;./gjsolver q1c/i0.6.26.user q1c/i0.6.26.user.sols 1> q1c/i0.6.26.user.out 2> q1c/i0.6.26.user.err 

276:
	mkdir q1c/i0.6.27.user.sols;./gjsolver q1c/i0.6.27.user q1c/i0.6.27.user.sols 1> q1c/i0.6.27.user.out 2> q1c/i0.6.27.user.err 

277:
	mkdir q1c/i0.6.28.user.sols;./gjsolver q1c/i0.6.28.user q1c/i0.6.28.user.sols 1> q1c/i0.6.28.user.out 2> q1c/i0.6.28.user.err 

278:
	mkdir q1c/i0.6.29.user.sols;./gjsolver q1c/i0.6.29.user q1c/i0.6.29.user.sols 1> q1c/i0.6.29.user.out 2> q1c/i0.6.29.user.err 

279:
	mkdir q1c/i0.6.3.user.sols;./gjsolver q1c/i0.6.3.user q1c/i0.6.3.user.sols 1> q1c/i0.6.3.user.out 2> q1c/i0.6.3.user.err 

280:
	mkdir q1c/i0.6.30.user.sols;./gjsolver q1c/i0.6.30.user q1c/i0.6.30.user.sols 1> q1c/i0.6.30.user.out 2> q1c/i0.6.30.user.err 

281:
	mkdir q1c/i0.6.4.user.sols;./gjsolver q1c/i0.6.4.user q1c/i0.6.4.user.sols 1> q1c/i0.6.4.user.out 2> q1c/i0.6.4.user.err 

282:
	mkdir q1c/i0.6.5.user.sols;./gjsolver q1c/i0.6.5.user q1c/i0.6.5.user.sols 1> q1c/i0.6.5.user.out 2> q1c/i0.6.5.user.err 

283:
	mkdir q1c/i0.6.6.user.sols;./gjsolver q1c/i0.6.6.user q1c/i0.6.6.user.sols 1> q1c/i0.6.6.user.out 2> q1c/i0.6.6.user.err 

284:
	mkdir q1c/i0.6.7.user.sols;./gjsolver q1c/i0.6.7.user q1c/i0.6.7.user.sols 1> q1c/i0.6.7.user.out 2> q1c/i0.6.7.user.err 

285:
	mkdir q1c/i0.6.8.user.sols;./gjsolver q1c/i0.6.8.user q1c/i0.6.8.user.sols 1> q1c/i0.6.8.user.out 2> q1c/i0.6.8.user.err 

286:
	mkdir q1c/i0.6.9.user.sols;./gjsolver q1c/i0.6.9.user q1c/i0.6.9.user.sols 1> q1c/i0.6.9.user.out 2> q1c/i0.6.9.user.err 

287:
	mkdir q1c/i0.7.1.user.sols;./gjsolver q1c/i0.7.1.user q1c/i0.7.1.user.sols 1> q1c/i0.7.1.user.out 2> q1c/i0.7.1.user.err 

288:
	mkdir q1c/i0.7.10.user.sols;./gjsolver q1c/i0.7.10.user q1c/i0.7.10.user.sols 1> q1c/i0.7.10.user.out 2> q1c/i0.7.10.user.err 

289:
	mkdir q1c/i0.7.11.user.sols;./gjsolver q1c/i0.7.11.user q1c/i0.7.11.user.sols 1> q1c/i0.7.11.user.out 2> q1c/i0.7.11.user.err 

290:
	mkdir q1c/i0.7.12.user.sols;./gjsolver q1c/i0.7.12.user q1c/i0.7.12.user.sols 1> q1c/i0.7.12.user.out 2> q1c/i0.7.12.user.err 

291:
	mkdir q1c/i0.7.13.user.sols;./gjsolver q1c/i0.7.13.user q1c/i0.7.13.user.sols 1> q1c/i0.7.13.user.out 2> q1c/i0.7.13.user.err 

292:
	mkdir q1c/i0.7.14.user.sols;./gjsolver q1c/i0.7.14.user q1c/i0.7.14.user.sols 1> q1c/i0.7.14.user.out 2> q1c/i0.7.14.user.err 

293:
	mkdir q1c/i0.7.15.user.sols;./gjsolver q1c/i0.7.15.user q1c/i0.7.15.user.sols 1> q1c/i0.7.15.user.out 2> q1c/i0.7.15.user.err 

294:
	mkdir q1c/i0.7.16.user.sols;./gjsolver q1c/i0.7.16.user q1c/i0.7.16.user.sols 1> q1c/i0.7.16.user.out 2> q1c/i0.7.16.user.err 

295:
	mkdir q1c/i0.7.17.user.sols;./gjsolver q1c/i0.7.17.user q1c/i0.7.17.user.sols 1> q1c/i0.7.17.user.out 2> q1c/i0.7.17.user.err 

296:
	mkdir q1c/i0.7.18.user.sols;./gjsolver q1c/i0.7.18.user q1c/i0.7.18.user.sols 1> q1c/i0.7.18.user.out 2> q1c/i0.7.18.user.err 

297:
	mkdir q1c/i0.7.19.user.sols;./gjsolver q1c/i0.7.19.user q1c/i0.7.19.user.sols 1> q1c/i0.7.19.user.out 2> q1c/i0.7.19.user.err 

298:
	mkdir q1c/i0.7.2.user.sols;./gjsolver q1c/i0.7.2.user q1c/i0.7.2.user.sols 1> q1c/i0.7.2.user.out 2> q1c/i0.7.2.user.err 

299:
	mkdir q1c/i0.7.20.user.sols;./gjsolver q1c/i0.7.20.user q1c/i0.7.20.user.sols 1> q1c/i0.7.20.user.out 2> q1c/i0.7.20.user.err 

300:
	mkdir q1c/i0.7.21.user.sols;./gjsolver q1c/i0.7.21.user q1c/i0.7.21.user.sols 1> q1c/i0.7.21.user.out 2> q1c/i0.7.21.user.err 

301:
	mkdir q1c/i0.7.22.user.sols;./gjsolver q1c/i0.7.22.user q1c/i0.7.22.user.sols 1> q1c/i0.7.22.user.out 2> q1c/i0.7.22.user.err 

302:
	mkdir q1c/i0.7.23.user.sols;./gjsolver q1c/i0.7.23.user q1c/i0.7.23.user.sols 1> q1c/i0.7.23.user.out 2> q1c/i0.7.23.user.err 

303:
	mkdir q1c/i0.7.24.user.sols;./gjsolver q1c/i0.7.24.user q1c/i0.7.24.user.sols 1> q1c/i0.7.24.user.out 2> q1c/i0.7.24.user.err 

304:
	mkdir q1c/i0.7.25.user.sols;./gjsolver q1c/i0.7.25.user q1c/i0.7.25.user.sols 1> q1c/i0.7.25.user.out 2> q1c/i0.7.25.user.err 

305:
	mkdir q1c/i0.7.26.user.sols;./gjsolver q1c/i0.7.26.user q1c/i0.7.26.user.sols 1> q1c/i0.7.26.user.out 2> q1c/i0.7.26.user.err 

306:
	mkdir q1c/i0.7.27.user.sols;./gjsolver q1c/i0.7.27.user q1c/i0.7.27.user.sols 1> q1c/i0.7.27.user.out 2> q1c/i0.7.27.user.err 

307:
	mkdir q1c/i0.7.28.user.sols;./gjsolver q1c/i0.7.28.user q1c/i0.7.28.user.sols 1> q1c/i0.7.28.user.out 2> q1c/i0.7.28.user.err 

308:
	mkdir q1c/i0.7.29.user.sols;./gjsolver q1c/i0.7.29.user q1c/i0.7.29.user.sols 1> q1c/i0.7.29.user.out 2> q1c/i0.7.29.user.err 

309:
	mkdir q1c/i0.7.3.user.sols;./gjsolver q1c/i0.7.3.user q1c/i0.7.3.user.sols 1> q1c/i0.7.3.user.out 2> q1c/i0.7.3.user.err 

310:
	mkdir q1c/i0.7.30.user.sols;./gjsolver q1c/i0.7.30.user q1c/i0.7.30.user.sols 1> q1c/i0.7.30.user.out 2> q1c/i0.7.30.user.err 

311:
	mkdir q1c/i0.7.4.user.sols;./gjsolver q1c/i0.7.4.user q1c/i0.7.4.user.sols 1> q1c/i0.7.4.user.out 2> q1c/i0.7.4.user.err 

312:
	mkdir q1c/i0.7.5.user.sols;./gjsolver q1c/i0.7.5.user q1c/i0.7.5.user.sols 1> q1c/i0.7.5.user.out 2> q1c/i0.7.5.user.err 

313:
	mkdir q1c/i0.7.6.user.sols;./gjsolver q1c/i0.7.6.user q1c/i0.7.6.user.sols 1> q1c/i0.7.6.user.out 2> q1c/i0.7.6.user.err 

314:
	mkdir q1c/i0.7.7.user.sols;./gjsolver q1c/i0.7.7.user q1c/i0.7.7.user.sols 1> q1c/i0.7.7.user.out 2> q1c/i0.7.7.user.err 

315:
	mkdir q1c/i0.7.8.user.sols;./gjsolver q1c/i0.7.8.user q1c/i0.7.8.user.sols 1> q1c/i0.7.8.user.out 2> q1c/i0.7.8.user.err 

316:
	mkdir q1c/i0.7.9.user.sols;./gjsolver q1c/i0.7.9.user q1c/i0.7.9.user.sols 1> q1c/i0.7.9.user.out 2> q1c/i0.7.9.user.err 

317:
	mkdir q1c/i0.8.1.user.sols;./gjsolver q1c/i0.8.1.user q1c/i0.8.1.user.sols 1> q1c/i0.8.1.user.out 2> q1c/i0.8.1.user.err 

318:
	mkdir q1c/i0.8.10.user.sols;./gjsolver q1c/i0.8.10.user q1c/i0.8.10.user.sols 1> q1c/i0.8.10.user.out 2> q1c/i0.8.10.user.err 

319:
	mkdir q1c/i0.8.11.user.sols;./gjsolver q1c/i0.8.11.user q1c/i0.8.11.user.sols 1> q1c/i0.8.11.user.out 2> q1c/i0.8.11.user.err 

320:
	mkdir q1c/i0.8.12.user.sols;./gjsolver q1c/i0.8.12.user q1c/i0.8.12.user.sols 1> q1c/i0.8.12.user.out 2> q1c/i0.8.12.user.err 

321:
	mkdir q1c/i0.8.13.user.sols;./gjsolver q1c/i0.8.13.user q1c/i0.8.13.user.sols 1> q1c/i0.8.13.user.out 2> q1c/i0.8.13.user.err 

322:
	mkdir q1c/i0.8.14.user.sols;./gjsolver q1c/i0.8.14.user q1c/i0.8.14.user.sols 1> q1c/i0.8.14.user.out 2> q1c/i0.8.14.user.err 

323:
	mkdir q1c/i0.8.15.user.sols;./gjsolver q1c/i0.8.15.user q1c/i0.8.15.user.sols 1> q1c/i0.8.15.user.out 2> q1c/i0.8.15.user.err 

324:
	mkdir q1c/i0.8.16.user.sols;./gjsolver q1c/i0.8.16.user q1c/i0.8.16.user.sols 1> q1c/i0.8.16.user.out 2> q1c/i0.8.16.user.err 

325:
	mkdir q1c/i0.8.17.user.sols;./gjsolver q1c/i0.8.17.user q1c/i0.8.17.user.sols 1> q1c/i0.8.17.user.out 2> q1c/i0.8.17.user.err 

326:
	mkdir q1c/i0.8.18.user.sols;./gjsolver q1c/i0.8.18.user q1c/i0.8.18.user.sols 1> q1c/i0.8.18.user.out 2> q1c/i0.8.18.user.err 

327:
	mkdir q1c/i0.8.19.user.sols;./gjsolver q1c/i0.8.19.user q1c/i0.8.19.user.sols 1> q1c/i0.8.19.user.out 2> q1c/i0.8.19.user.err 

328:
	mkdir q1c/i0.8.2.user.sols;./gjsolver q1c/i0.8.2.user q1c/i0.8.2.user.sols 1> q1c/i0.8.2.user.out 2> q1c/i0.8.2.user.err 

329:
	mkdir q1c/i0.8.20.user.sols;./gjsolver q1c/i0.8.20.user q1c/i0.8.20.user.sols 1> q1c/i0.8.20.user.out 2> q1c/i0.8.20.user.err 

330:
	mkdir q1c/i0.8.21.user.sols;./gjsolver q1c/i0.8.21.user q1c/i0.8.21.user.sols 1> q1c/i0.8.21.user.out 2> q1c/i0.8.21.user.err 

331:
	mkdir q1c/i0.8.22.user.sols;./gjsolver q1c/i0.8.22.user q1c/i0.8.22.user.sols 1> q1c/i0.8.22.user.out 2> q1c/i0.8.22.user.err 

332:
	mkdir q1c/i0.8.23.user.sols;./gjsolver q1c/i0.8.23.user q1c/i0.8.23.user.sols 1> q1c/i0.8.23.user.out 2> q1c/i0.8.23.user.err 

333:
	mkdir q1c/i0.8.24.user.sols;./gjsolver q1c/i0.8.24.user q1c/i0.8.24.user.sols 1> q1c/i0.8.24.user.out 2> q1c/i0.8.24.user.err 

334:
	mkdir q1c/i0.8.25.user.sols;./gjsolver q1c/i0.8.25.user q1c/i0.8.25.user.sols 1> q1c/i0.8.25.user.out 2> q1c/i0.8.25.user.err 

335:
	mkdir q1c/i0.8.26.user.sols;./gjsolver q1c/i0.8.26.user q1c/i0.8.26.user.sols 1> q1c/i0.8.26.user.out 2> q1c/i0.8.26.user.err 

336:
	mkdir q1c/i0.8.27.user.sols;./gjsolver q1c/i0.8.27.user q1c/i0.8.27.user.sols 1> q1c/i0.8.27.user.out 2> q1c/i0.8.27.user.err 

337:
	mkdir q1c/i0.8.28.user.sols;./gjsolver q1c/i0.8.28.user q1c/i0.8.28.user.sols 1> q1c/i0.8.28.user.out 2> q1c/i0.8.28.user.err 

338:
	mkdir q1c/i0.8.29.user.sols;./gjsolver q1c/i0.8.29.user q1c/i0.8.29.user.sols 1> q1c/i0.8.29.user.out 2> q1c/i0.8.29.user.err 

339:
	mkdir q1c/i0.8.3.user.sols;./gjsolver q1c/i0.8.3.user q1c/i0.8.3.user.sols 1> q1c/i0.8.3.user.out 2> q1c/i0.8.3.user.err 

340:
	mkdir q1c/i0.8.30.user.sols;./gjsolver q1c/i0.8.30.user q1c/i0.8.30.user.sols 1> q1c/i0.8.30.user.out 2> q1c/i0.8.30.user.err 

341:
	mkdir q1c/i0.8.4.user.sols;./gjsolver q1c/i0.8.4.user q1c/i0.8.4.user.sols 1> q1c/i0.8.4.user.out 2> q1c/i0.8.4.user.err 

342:
	mkdir q1c/i0.8.5.user.sols;./gjsolver q1c/i0.8.5.user q1c/i0.8.5.user.sols 1> q1c/i0.8.5.user.out 2> q1c/i0.8.5.user.err 

343:
	mkdir q1c/i0.8.6.user.sols;./gjsolver q1c/i0.8.6.user q1c/i0.8.6.user.sols 1> q1c/i0.8.6.user.out 2> q1c/i0.8.6.user.err 

344:
	mkdir q1c/i0.8.7.user.sols;./gjsolver q1c/i0.8.7.user q1c/i0.8.7.user.sols 1> q1c/i0.8.7.user.out 2> q1c/i0.8.7.user.err 

345:
	mkdir q1c/i0.8.8.user.sols;./gjsolver q1c/i0.8.8.user q1c/i0.8.8.user.sols 1> q1c/i0.8.8.user.out 2> q1c/i0.8.8.user.err 

346:
	mkdir q1c/i0.8.9.user.sols;./gjsolver q1c/i0.8.9.user q1c/i0.8.9.user.sols 1> q1c/i0.8.9.user.out 2> q1c/i0.8.9.user.err 

347:
	mkdir q1c/i0.9.1.user.sols;./gjsolver q1c/i0.9.1.user q1c/i0.9.1.user.sols 1> q1c/i0.9.1.user.out 2> q1c/i0.9.1.user.err 

348:
	mkdir q1c/i0.9.10.user.sols;./gjsolver q1c/i0.9.10.user q1c/i0.9.10.user.sols 1> q1c/i0.9.10.user.out 2> q1c/i0.9.10.user.err 

349:
	mkdir q1c/i0.9.11.user.sols;./gjsolver q1c/i0.9.11.user q1c/i0.9.11.user.sols 1> q1c/i0.9.11.user.out 2> q1c/i0.9.11.user.err 

350:
	mkdir q1c/i0.9.12.user.sols;./gjsolver q1c/i0.9.12.user q1c/i0.9.12.user.sols 1> q1c/i0.9.12.user.out 2> q1c/i0.9.12.user.err 

351:
	mkdir q1c/i0.9.13.user.sols;./gjsolver q1c/i0.9.13.user q1c/i0.9.13.user.sols 1> q1c/i0.9.13.user.out 2> q1c/i0.9.13.user.err 

352:
	mkdir q1c/i0.9.14.user.sols;./gjsolver q1c/i0.9.14.user q1c/i0.9.14.user.sols 1> q1c/i0.9.14.user.out 2> q1c/i0.9.14.user.err 

353:
	mkdir q1c/i0.9.15.user.sols;./gjsolver q1c/i0.9.15.user q1c/i0.9.15.user.sols 1> q1c/i0.9.15.user.out 2> q1c/i0.9.15.user.err 

354:
	mkdir q1c/i0.9.16.user.sols;./gjsolver q1c/i0.9.16.user q1c/i0.9.16.user.sols 1> q1c/i0.9.16.user.out 2> q1c/i0.9.16.user.err 

355:
	mkdir q1c/i0.9.17.user.sols;./gjsolver q1c/i0.9.17.user q1c/i0.9.17.user.sols 1> q1c/i0.9.17.user.out 2> q1c/i0.9.17.user.err 

356:
	mkdir q1c/i0.9.18.user.sols;./gjsolver q1c/i0.9.18.user q1c/i0.9.18.user.sols 1> q1c/i0.9.18.user.out 2> q1c/i0.9.18.user.err 

357:
	mkdir q1c/i0.9.19.user.sols;./gjsolver q1c/i0.9.19.user q1c/i0.9.19.user.sols 1> q1c/i0.9.19.user.out 2> q1c/i0.9.19.user.err 

358:
	mkdir q1c/i0.9.2.user.sols;./gjsolver q1c/i0.9.2.user q1c/i0.9.2.user.sols 1> q1c/i0.9.2.user.out 2> q1c/i0.9.2.user.err 

359:
	mkdir q1c/i0.9.20.user.sols;./gjsolver q1c/i0.9.20.user q1c/i0.9.20.user.sols 1> q1c/i0.9.20.user.out 2> q1c/i0.9.20.user.err 

360:
	mkdir q1c/i0.9.21.user.sols;./gjsolver q1c/i0.9.21.user q1c/i0.9.21.user.sols 1> q1c/i0.9.21.user.out 2> q1c/i0.9.21.user.err 

361:
	mkdir q1c/i0.9.22.user.sols;./gjsolver q1c/i0.9.22.user q1c/i0.9.22.user.sols 1> q1c/i0.9.22.user.out 2> q1c/i0.9.22.user.err 

362:
	mkdir q1c/i0.9.23.user.sols;./gjsolver q1c/i0.9.23.user q1c/i0.9.23.user.sols 1> q1c/i0.9.23.user.out 2> q1c/i0.9.23.user.err 

363:
	mkdir q1c/i0.9.24.user.sols;./gjsolver q1c/i0.9.24.user q1c/i0.9.24.user.sols 1> q1c/i0.9.24.user.out 2> q1c/i0.9.24.user.err 

364:
	mkdir q1c/i0.9.25.user.sols;./gjsolver q1c/i0.9.25.user q1c/i0.9.25.user.sols 1> q1c/i0.9.25.user.out 2> q1c/i0.9.25.user.err 

365:
	mkdir q1c/i0.9.26.user.sols;./gjsolver q1c/i0.9.26.user q1c/i0.9.26.user.sols 1> q1c/i0.9.26.user.out 2> q1c/i0.9.26.user.err 

366:
	mkdir q1c/i0.9.27.user.sols;./gjsolver q1c/i0.9.27.user q1c/i0.9.27.user.sols 1> q1c/i0.9.27.user.out 2> q1c/i0.9.27.user.err 

367:
	mkdir q1c/i0.9.28.user.sols;./gjsolver q1c/i0.9.28.user q1c/i0.9.28.user.sols 1> q1c/i0.9.28.user.out 2> q1c/i0.9.28.user.err 

368:
	mkdir q1c/i0.9.29.user.sols;./gjsolver q1c/i0.9.29.user q1c/i0.9.29.user.sols 1> q1c/i0.9.29.user.out 2> q1c/i0.9.29.user.err 

369:
	mkdir q1c/i0.9.3.user.sols;./gjsolver q1c/i0.9.3.user q1c/i0.9.3.user.sols 1> q1c/i0.9.3.user.out 2> q1c/i0.9.3.user.err 

370:
	mkdir q1c/i0.9.30.user.sols;./gjsolver q1c/i0.9.30.user q1c/i0.9.30.user.sols 1> q1c/i0.9.30.user.out 2> q1c/i0.9.30.user.err 

371:
	mkdir q1c/i0.9.4.user.sols;./gjsolver q1c/i0.9.4.user q1c/i0.9.4.user.sols 1> q1c/i0.9.4.user.out 2> q1c/i0.9.4.user.err 

372:
	mkdir q1c/i0.9.5.user.sols;./gjsolver q1c/i0.9.5.user q1c/i0.9.5.user.sols 1> q1c/i0.9.5.user.out 2> q1c/i0.9.5.user.err 

373:
	mkdir q1c/i0.9.6.user.sols;./gjsolver q1c/i0.9.6.user q1c/i0.9.6.user.sols 1> q1c/i0.9.6.user.out 2> q1c/i0.9.6.user.err 

374:
	mkdir q1c/i0.9.7.user.sols;./gjsolver q1c/i0.9.7.user q1c/i0.9.7.user.sols 1> q1c/i0.9.7.user.out 2> q1c/i0.9.7.user.err 

375:
	mkdir q1c/i0.9.8.user.sols;./gjsolver q1c/i0.9.8.user q1c/i0.9.8.user.sols 1> q1c/i0.9.8.user.out 2> q1c/i0.9.8.user.err 

376:
	mkdir q1c/i0.9.9.user.sols;./gjsolver q1c/i0.9.9.user q1c/i0.9.9.user.sols 1> q1c/i0.9.9.user.out 2> q1c/i0.9.9.user.err 

377:
	mkdir q2c/i1.user.sols;./gjsolver q2c/i1.user q2c/i1.user.sols 1> q2c/i1.user.out 2> q2c/i1.user.err 

378:
	mkdir q2c/i10.user.sols;./gjsolver q2c/i10.user q2c/i10.user.sols 1> q2c/i10.user.out 2> q2c/i10.user.err 

379:
	mkdir q2c/i11.user.sols;./gjsolver q2c/i11.user q2c/i11.user.sols 1> q2c/i11.user.out 2> q2c/i11.user.err 

380:
	mkdir q2c/i12.user.sols;./gjsolver q2c/i12.user q2c/i12.user.sols 1> q2c/i12.user.out 2> q2c/i12.user.err 

381:
	mkdir q2c/i13.user.sols;./gjsolver q2c/i13.user q2c/i13.user.sols 1> q2c/i13.user.out 2> q2c/i13.user.err 

382:
	mkdir q2c/i14.user.sols;./gjsolver q2c/i14.user q2c/i14.user.sols 1> q2c/i14.user.out 2> q2c/i14.user.err 

383:
	mkdir q2c/i15.user.sols;./gjsolver q2c/i15.user q2c/i15.user.sols 1> q2c/i15.user.out 2> q2c/i15.user.err 

384:
	mkdir q2c/i16.user.sols;./gjsolver q2c/i16.user q2c/i16.user.sols 1> q2c/i16.user.out 2> q2c/i16.user.err 

385:
	mkdir q2c/i17.user.sols;./gjsolver q2c/i17.user q2c/i17.user.sols 1> q2c/i17.user.out 2> q2c/i17.user.err 

386:
	mkdir q2c/i18.user.sols;./gjsolver q2c/i18.user q2c/i18.user.sols 1> q2c/i18.user.out 2> q2c/i18.user.err 

387:
	mkdir q2c/i19.user.sols;./gjsolver q2c/i19.user q2c/i19.user.sols 1> q2c/i19.user.out 2> q2c/i19.user.err 

388:
	mkdir q2c/i2.user.sols;./gjsolver q2c/i2.user q2c/i2.user.sols 1> q2c/i2.user.out 2> q2c/i2.user.err 

389:
	mkdir q2c/i20.user.sols;./gjsolver q2c/i20.user q2c/i20.user.sols 1> q2c/i20.user.out 2> q2c/i20.user.err 

390:
	mkdir q2c/i21.user.sols;./gjsolver q2c/i21.user q2c/i21.user.sols 1> q2c/i21.user.out 2> q2c/i21.user.err 

391:
	mkdir q2c/i22.user.sols;./gjsolver q2c/i22.user q2c/i22.user.sols 1> q2c/i22.user.out 2> q2c/i22.user.err 

392:
	mkdir q2c/i23.user.sols;./gjsolver q2c/i23.user q2c/i23.user.sols 1> q2c/i23.user.out 2> q2c/i23.user.err 

393:
	mkdir q2c/i24.user.sols;./gjsolver q2c/i24.user q2c/i24.user.sols 1> q2c/i24.user.out 2> q2c/i24.user.err 

394:
	mkdir q2c/i25.user.sols;./gjsolver q2c/i25.user q2c/i25.user.sols 1> q2c/i25.user.out 2> q2c/i25.user.err 

395:
	mkdir q2c/i26.user.sols;./gjsolver q2c/i26.user q2c/i26.user.sols 1> q2c/i26.user.out 2> q2c/i26.user.err 

396:
	mkdir q2c/i27.user.sols;./gjsolver q2c/i27.user q2c/i27.user.sols 1> q2c/i27.user.out 2> q2c/i27.user.err 

397:
	mkdir q2c/i28.user.sols;./gjsolver q2c/i28.user q2c/i28.user.sols 1> q2c/i28.user.out 2> q2c/i28.user.err 

398:
	mkdir q2c/i29.user.sols;./gjsolver q2c/i29.user q2c/i29.user.sols 1> q2c/i29.user.out 2> q2c/i29.user.err 

399:
	mkdir q2c/i3.user.sols;./gjsolver q2c/i3.user q2c/i3.user.sols 1> q2c/i3.user.out 2> q2c/i3.user.err 

400:
	mkdir q2c/i30.user.sols;./gjsolver q2c/i30.user q2c/i30.user.sols 1> q2c/i30.user.out 2> q2c/i30.user.err 

401:
	mkdir q2c/i4.user.sols;./gjsolver q2c/i4.user q2c/i4.user.sols 1> q2c/i4.user.out 2> q2c/i4.user.err 

402:
	mkdir q2c/i5.user.sols;./gjsolver q2c/i5.user q2c/i5.user.sols 1> q2c/i5.user.out 2> q2c/i5.user.err 

403:
	mkdir q2c/i6.user.sols;./gjsolver q2c/i6.user q2c/i6.user.sols 1> q2c/i6.user.out 2> q2c/i6.user.err 

404:
	mkdir q2c/i7.user.sols;./gjsolver q2c/i7.user q2c/i7.user.sols 1> q2c/i7.user.out 2> q2c/i7.user.err 

405:
	mkdir q2c/i8.user.sols;./gjsolver q2c/i8.user q2c/i8.user.sols 1> q2c/i8.user.out 2> q2c/i8.user.err 

406:
	mkdir q2c/i9.user.sols;./gjsolver q2c/i9.user q2c/i9.user.sols 1> q2c/i9.user.out 2> q2c/i9.user.err 

407:
	mkdir q3/exinstall-1.user.sols;./gjsolver q3/exinstall-1.user q3/exinstall-1.user.sols 1> q3/exinstall-1.user.out 2> q3/exinstall-1.user.err 

408:
	mkdir q3/exinstall-10.user.sols;./gjsolver q3/exinstall-10.user q3/exinstall-10.user.sols 1> q3/exinstall-10.user.out 2> q3/exinstall-10.user.err 

409:
	mkdir q3/exinstall-11.user.sols;./gjsolver q3/exinstall-11.user q3/exinstall-11.user.sols 1> q3/exinstall-11.user.out 2> q3/exinstall-11.user.err 

410:
	mkdir q3/exinstall-12.user.sols;./gjsolver q3/exinstall-12.user q3/exinstall-12.user.sols 1> q3/exinstall-12.user.out 2> q3/exinstall-12.user.err 

411:
	mkdir q3/exinstall-13.user.sols;./gjsolver q3/exinstall-13.user q3/exinstall-13.user.sols 1> q3/exinstall-13.user.out 2> q3/exinstall-13.user.err 

412:
	mkdir q3/exinstall-14.user.sols;./gjsolver q3/exinstall-14.user q3/exinstall-14.user.sols 1> q3/exinstall-14.user.out 2> q3/exinstall-14.user.err 

413:
	mkdir q3/exinstall-15.user.sols;./gjsolver q3/exinstall-15.user q3/exinstall-15.user.sols 1> q3/exinstall-15.user.out 2> q3/exinstall-15.user.err 

414:
	mkdir q3/exinstall-16.user.sols;./gjsolver q3/exinstall-16.user q3/exinstall-16.user.sols 1> q3/exinstall-16.user.out 2> q3/exinstall-16.user.err 

415:
	mkdir q3/exinstall-17.user.sols;./gjsolver q3/exinstall-17.user q3/exinstall-17.user.sols 1> q3/exinstall-17.user.out 2> q3/exinstall-17.user.err 

416:
	mkdir q3/exinstall-18.user.sols;./gjsolver q3/exinstall-18.user q3/exinstall-18.user.sols 1> q3/exinstall-18.user.out 2> q3/exinstall-18.user.err 

417:
	mkdir q3/exinstall-19.user.sols;./gjsolver q3/exinstall-19.user q3/exinstall-19.user.sols 1> q3/exinstall-19.user.out 2> q3/exinstall-19.user.err 

418:
	mkdir q3/exinstall-2.user.sols;./gjsolver q3/exinstall-2.user q3/exinstall-2.user.sols 1> q3/exinstall-2.user.out 2> q3/exinstall-2.user.err 

419:
	mkdir q3/exinstall-20.user.sols;./gjsolver q3/exinstall-20.user q3/exinstall-20.user.sols 1> q3/exinstall-20.user.out 2> q3/exinstall-20.user.err 

420:
	mkdir q3/exinstall-21.user.sols;./gjsolver q3/exinstall-21.user q3/exinstall-21.user.sols 1> q3/exinstall-21.user.out 2> q3/exinstall-21.user.err 

421:
	mkdir q3/exinstall-22.user.sols;./gjsolver q3/exinstall-22.user q3/exinstall-22.user.sols 1> q3/exinstall-22.user.out 2> q3/exinstall-22.user.err 

422:
	mkdir q3/exinstall-23.user.sols;./gjsolver q3/exinstall-23.user q3/exinstall-23.user.sols 1> q3/exinstall-23.user.out 2> q3/exinstall-23.user.err 

423:
	mkdir q3/exinstall-24.user.sols;./gjsolver q3/exinstall-24.user q3/exinstall-24.user.sols 1> q3/exinstall-24.user.out 2> q3/exinstall-24.user.err 

424:
	mkdir q3/exinstall-25.user.sols;./gjsolver q3/exinstall-25.user q3/exinstall-25.user.sols 1> q3/exinstall-25.user.out 2> q3/exinstall-25.user.err 

425:
	mkdir q3/exinstall-26.user.sols;./gjsolver q3/exinstall-26.user q3/exinstall-26.user.sols 1> q3/exinstall-26.user.out 2> q3/exinstall-26.user.err 

426:
	mkdir q3/exinstall-27.user.sols;./gjsolver q3/exinstall-27.user q3/exinstall-27.user.sols 1> q3/exinstall-27.user.out 2> q3/exinstall-27.user.err 

427:
	mkdir q3/exinstall-28.user.sols;./gjsolver q3/exinstall-28.user q3/exinstall-28.user.sols 1> q3/exinstall-28.user.out 2> q3/exinstall-28.user.err 

428:
	mkdir q3/exinstall-29.user.sols;./gjsolver q3/exinstall-29.user q3/exinstall-29.user.sols 1> q3/exinstall-29.user.out 2> q3/exinstall-29.user.err 

429:
	mkdir q3/exinstall-3.user.sols;./gjsolver q3/exinstall-3.user q3/exinstall-3.user.sols 1> q3/exinstall-3.user.out 2> q3/exinstall-3.user.err 

430:
	mkdir q3/exinstall-30.user.sols;./gjsolver q3/exinstall-30.user q3/exinstall-30.user.sols 1> q3/exinstall-30.user.out 2> q3/exinstall-30.user.err 

431:
	mkdir q3/exinstall-4.user.sols;./gjsolver q3/exinstall-4.user q3/exinstall-4.user.sols 1> q3/exinstall-4.user.out 2> q3/exinstall-4.user.err 

432:
	mkdir q3/exinstall-5.user.sols;./gjsolver q3/exinstall-5.user q3/exinstall-5.user.sols 1> q3/exinstall-5.user.out 2> q3/exinstall-5.user.err 

433:
	mkdir q3/exinstall-6.user.sols;./gjsolver q3/exinstall-6.user q3/exinstall-6.user.sols 1> q3/exinstall-6.user.out 2> q3/exinstall-6.user.err 

434:
	mkdir q3/exinstall-7.user.sols;./gjsolver q3/exinstall-7.user q3/exinstall-7.user.sols 1> q3/exinstall-7.user.out 2> q3/exinstall-7.user.err 

435:
	mkdir q3/exinstall-8.user.sols;./gjsolver q3/exinstall-8.user q3/exinstall-8.user.sols 1> q3/exinstall-8.user.out 2> q3/exinstall-8.user.err 

436:
	mkdir q3/exinstall-9.user.sols;./gjsolver q3/exinstall-9.user q3/exinstall-9.user.sols 1> q3/exinstall-9.user.out 2> q3/exinstall-9.user.err 

437:
	mkdir q3/exupdate-1.user.sols;./gjsolver q3/exupdate-1.user q3/exupdate-1.user.sols 1> q3/exupdate-1.user.out 2> q3/exupdate-1.user.err 

438:
	mkdir q3/exupdate-10.user.sols;./gjsolver q3/exupdate-10.user q3/exupdate-10.user.sols 1> q3/exupdate-10.user.out 2> q3/exupdate-10.user.err 

439:
	mkdir q3/exupdate-11.user.sols;./gjsolver q3/exupdate-11.user q3/exupdate-11.user.sols 1> q3/exupdate-11.user.out 2> q3/exupdate-11.user.err 

440:
	mkdir q3/exupdate-12.user.sols;./gjsolver q3/exupdate-12.user q3/exupdate-12.user.sols 1> q3/exupdate-12.user.out 2> q3/exupdate-12.user.err 

441:
	mkdir q3/exupdate-13.user.sols;./gjsolver q3/exupdate-13.user q3/exupdate-13.user.sols 1> q3/exupdate-13.user.out 2> q3/exupdate-13.user.err 

442:
	mkdir q3/exupdate-14.user.sols;./gjsolver q3/exupdate-14.user q3/exupdate-14.user.sols 1> q3/exupdate-14.user.out 2> q3/exupdate-14.user.err 

443:
	mkdir q3/exupdate-15.user.sols;./gjsolver q3/exupdate-15.user q3/exupdate-15.user.sols 1> q3/exupdate-15.user.out 2> q3/exupdate-15.user.err 

444:
	mkdir q3/exupdate-16.user.sols;./gjsolver q3/exupdate-16.user q3/exupdate-16.user.sols 1> q3/exupdate-16.user.out 2> q3/exupdate-16.user.err 

445:
	mkdir q3/exupdate-17.user.sols;./gjsolver q3/exupdate-17.user q3/exupdate-17.user.sols 1> q3/exupdate-17.user.out 2> q3/exupdate-17.user.err 

446:
	mkdir q3/exupdate-18.user.sols;./gjsolver q3/exupdate-18.user q3/exupdate-18.user.sols 1> q3/exupdate-18.user.out 2> q3/exupdate-18.user.err 

447:
	mkdir q3/exupdate-19.user.sols;./gjsolver q3/exupdate-19.user q3/exupdate-19.user.sols 1> q3/exupdate-19.user.out 2> q3/exupdate-19.user.err 

448:
	mkdir q3/exupdate-2.user.sols;./gjsolver q3/exupdate-2.user q3/exupdate-2.user.sols 1> q3/exupdate-2.user.out 2> q3/exupdate-2.user.err 

449:
	mkdir q3/exupdate-20.user.sols;./gjsolver q3/exupdate-20.user q3/exupdate-20.user.sols 1> q3/exupdate-20.user.out 2> q3/exupdate-20.user.err 

450:
	mkdir q3/exupdate-21.user.sols;./gjsolver q3/exupdate-21.user q3/exupdate-21.user.sols 1> q3/exupdate-21.user.out 2> q3/exupdate-21.user.err 

451:
	mkdir q3/exupdate-22.user.sols;./gjsolver q3/exupdate-22.user q3/exupdate-22.user.sols 1> q3/exupdate-22.user.out 2> q3/exupdate-22.user.err 

452:
	mkdir q3/exupdate-23.user.sols;./gjsolver q3/exupdate-23.user q3/exupdate-23.user.sols 1> q3/exupdate-23.user.out 2> q3/exupdate-23.user.err 

453:
	mkdir q3/exupdate-24.user.sols;./gjsolver q3/exupdate-24.user q3/exupdate-24.user.sols 1> q3/exupdate-24.user.out 2> q3/exupdate-24.user.err 

454:
	mkdir q3/exupdate-25.user.sols;./gjsolver q3/exupdate-25.user q3/exupdate-25.user.sols 1> q3/exupdate-25.user.out 2> q3/exupdate-25.user.err 

455:
	mkdir q3/exupdate-26.user.sols;./gjsolver q3/exupdate-26.user q3/exupdate-26.user.sols 1> q3/exupdate-26.user.out 2> q3/exupdate-26.user.err 

456:
	mkdir q3/exupdate-27.user.sols;./gjsolver q3/exupdate-27.user q3/exupdate-27.user.sols 1> q3/exupdate-27.user.out 2> q3/exupdate-27.user.err 

457:
	mkdir q3/exupdate-28.user.sols;./gjsolver q3/exupdate-28.user q3/exupdate-28.user.sols 1> q3/exupdate-28.user.out 2> q3/exupdate-28.user.err 

458:
	mkdir q3/exupdate-29.user.sols;./gjsolver q3/exupdate-29.user q3/exupdate-29.user.sols 1> q3/exupdate-29.user.out 2> q3/exupdate-29.user.err 

459:
	mkdir q3/exupdate-3.user.sols;./gjsolver q3/exupdate-3.user q3/exupdate-3.user.sols 1> q3/exupdate-3.user.out 2> q3/exupdate-3.user.err 

460:
	mkdir q3/exupdate-30.user.sols;./gjsolver q3/exupdate-30.user q3/exupdate-30.user.sols 1> q3/exupdate-30.user.out 2> q3/exupdate-30.user.err 

461:
	mkdir q3/exupdate-4.user.sols;./gjsolver q3/exupdate-4.user q3/exupdate-4.user.sols 1> q3/exupdate-4.user.out 2> q3/exupdate-4.user.err 

462:
	mkdir q3/exupdate-5.user.sols;./gjsolver q3/exupdate-5.user q3/exupdate-5.user.sols 1> q3/exupdate-5.user.out 2> q3/exupdate-5.user.err 

463:
	mkdir q3/exupdate-6.user.sols;./gjsolver q3/exupdate-6.user q3/exupdate-6.user.sols 1> q3/exupdate-6.user.out 2> q3/exupdate-6.user.err 

464:
	mkdir q3/exupdate-7.user.sols;./gjsolver q3/exupdate-7.user q3/exupdate-7.user.sols 1> q3/exupdate-7.user.out 2> q3/exupdate-7.user.err 

465:
	mkdir q3/exupdate-8.user.sols;./gjsolver q3/exupdate-8.user q3/exupdate-8.user.sols 1> q3/exupdate-8.user.out 2> q3/exupdate-8.user.err 

466:
	mkdir q3/exupdate-9.user.sols;./gjsolver q3/exupdate-9.user q3/exupdate-9.user.sols 1> q3/exupdate-9.user.out 2> q3/exupdate-9.user.err 

467:
	mkdir q3/u1-1.user.sols;./gjsolver q3/u1-1.user q3/u1-1.user.sols 1> q3/u1-1.user.out 2> q3/u1-1.user.err 

468:
	mkdir q3/u1-10.user.sols;./gjsolver q3/u1-10.user q3/u1-10.user.sols 1> q3/u1-10.user.out 2> q3/u1-10.user.err 

469:
	mkdir q3/u1-11.user.sols;./gjsolver q3/u1-11.user q3/u1-11.user.sols 1> q3/u1-11.user.out 2> q3/u1-11.user.err 

470:
	mkdir q3/u1-12.user.sols;./gjsolver q3/u1-12.user q3/u1-12.user.sols 1> q3/u1-12.user.out 2> q3/u1-12.user.err 

471:
	mkdir q3/u1-13.user.sols;./gjsolver q3/u1-13.user q3/u1-13.user.sols 1> q3/u1-13.user.out 2> q3/u1-13.user.err 

472:
	mkdir q3/u1-14.user.sols;./gjsolver q3/u1-14.user q3/u1-14.user.sols 1> q3/u1-14.user.out 2> q3/u1-14.user.err 

473:
	mkdir q3/u1-15.user.sols;./gjsolver q3/u1-15.user q3/u1-15.user.sols 1> q3/u1-15.user.out 2> q3/u1-15.user.err 

474:
	mkdir q3/u1-16.user.sols;./gjsolver q3/u1-16.user q3/u1-16.user.sols 1> q3/u1-16.user.out 2> q3/u1-16.user.err 

475:
	mkdir q3/u1-17.user.sols;./gjsolver q3/u1-17.user q3/u1-17.user.sols 1> q3/u1-17.user.out 2> q3/u1-17.user.err 

476:
	mkdir q3/u1-18.user.sols;./gjsolver q3/u1-18.user q3/u1-18.user.sols 1> q3/u1-18.user.out 2> q3/u1-18.user.err 

477:
	mkdir q3/u1-19.user.sols;./gjsolver q3/u1-19.user q3/u1-19.user.sols 1> q3/u1-19.user.out 2> q3/u1-19.user.err 

478:
	mkdir q3/u1-2.user.sols;./gjsolver q3/u1-2.user q3/u1-2.user.sols 1> q3/u1-2.user.out 2> q3/u1-2.user.err 

479:
	mkdir q3/u1-20.user.sols;./gjsolver q3/u1-20.user q3/u1-20.user.sols 1> q3/u1-20.user.out 2> q3/u1-20.user.err 

480:
	mkdir q3/u1-21.user.sols;./gjsolver q3/u1-21.user q3/u1-21.user.sols 1> q3/u1-21.user.out 2> q3/u1-21.user.err 

481:
	mkdir q3/u1-22.user.sols;./gjsolver q3/u1-22.user q3/u1-22.user.sols 1> q3/u1-22.user.out 2> q3/u1-22.user.err 

482:
	mkdir q3/u1-23.user.sols;./gjsolver q3/u1-23.user q3/u1-23.user.sols 1> q3/u1-23.user.out 2> q3/u1-23.user.err 

483:
	mkdir q3/u1-24.user.sols;./gjsolver q3/u1-24.user q3/u1-24.user.sols 1> q3/u1-24.user.out 2> q3/u1-24.user.err 

484:
	mkdir q3/u1-25.user.sols;./gjsolver q3/u1-25.user q3/u1-25.user.sols 1> q3/u1-25.user.out 2> q3/u1-25.user.err 

485:
	mkdir q3/u1-26.user.sols;./gjsolver q3/u1-26.user q3/u1-26.user.sols 1> q3/u1-26.user.out 2> q3/u1-26.user.err 

486:
	mkdir q3/u1-27.user.sols;./gjsolver q3/u1-27.user q3/u1-27.user.sols 1> q3/u1-27.user.out 2> q3/u1-27.user.err 

487:
	mkdir q3/u1-28.user.sols;./gjsolver q3/u1-28.user q3/u1-28.user.sols 1> q3/u1-28.user.out 2> q3/u1-28.user.err 

488:
	mkdir q3/u1-29.user.sols;./gjsolver q3/u1-29.user q3/u1-29.user.sols 1> q3/u1-29.user.out 2> q3/u1-29.user.err 

489:
	mkdir q3/u1-3.user.sols;./gjsolver q3/u1-3.user q3/u1-3.user.sols 1> q3/u1-3.user.out 2> q3/u1-3.user.err 

490:
	mkdir q3/u1-30.user.sols;./gjsolver q3/u1-30.user q3/u1-30.user.sols 1> q3/u1-30.user.out 2> q3/u1-30.user.err 

491:
	mkdir q3/u1-4.user.sols;./gjsolver q3/u1-4.user q3/u1-4.user.sols 1> q3/u1-4.user.out 2> q3/u1-4.user.err 

492:
	mkdir q3/u1-5.user.sols;./gjsolver q3/u1-5.user q3/u1-5.user.sols 1> q3/u1-5.user.out 2> q3/u1-5.user.err 

493:
	mkdir q3/u1-6.user.sols;./gjsolver q3/u1-6.user q3/u1-6.user.sols 1> q3/u1-6.user.out 2> q3/u1-6.user.err 

494:
	mkdir q3/u1-7.user.sols;./gjsolver q3/u1-7.user q3/u1-7.user.sols 1> q3/u1-7.user.out 2> q3/u1-7.user.err 

495:
	mkdir q3/u1-8.user.sols;./gjsolver q3/u1-8.user q3/u1-8.user.sols 1> q3/u1-8.user.out 2> q3/u1-8.user.err 

496:
	mkdir q3/u1-9.user.sols;./gjsolver q3/u1-9.user q3/u1-9.user.sols 1> q3/u1-9.user.out 2> q3/u1-9.user.err 

497:
	mkdir q3/u2-1.user.sols;./gjsolver q3/u2-1.user q3/u2-1.user.sols 1> q3/u2-1.user.out 2> q3/u2-1.user.err 

498:
	mkdir q3/u2-10.user.sols;./gjsolver q3/u2-10.user q3/u2-10.user.sols 1> q3/u2-10.user.out 2> q3/u2-10.user.err 

499:
	mkdir q3/u2-11.user.sols;./gjsolver q3/u2-11.user q3/u2-11.user.sols 1> q3/u2-11.user.out 2> q3/u2-11.user.err 

500:
	mkdir q3/u2-12.user.sols;./gjsolver q3/u2-12.user q3/u2-12.user.sols 1> q3/u2-12.user.out 2> q3/u2-12.user.err 

501:
	mkdir q3/u2-13.user.sols;./gjsolver q3/u2-13.user q3/u2-13.user.sols 1> q3/u2-13.user.out 2> q3/u2-13.user.err 

502:
	mkdir q3/u2-14.user.sols;./gjsolver q3/u2-14.user q3/u2-14.user.sols 1> q3/u2-14.user.out 2> q3/u2-14.user.err 

503:
	mkdir q3/u2-15.user.sols;./gjsolver q3/u2-15.user q3/u2-15.user.sols 1> q3/u2-15.user.out 2> q3/u2-15.user.err 

504:
	mkdir q3/u2-16.user.sols;./gjsolver q3/u2-16.user q3/u2-16.user.sols 1> q3/u2-16.user.out 2> q3/u2-16.user.err 

505:
	mkdir q3/u2-17.user.sols;./gjsolver q3/u2-17.user q3/u2-17.user.sols 1> q3/u2-17.user.out 2> q3/u2-17.user.err 

506:
	mkdir q3/u2-18.user.sols;./gjsolver q3/u2-18.user q3/u2-18.user.sols 1> q3/u2-18.user.out 2> q3/u2-18.user.err 

507:
	mkdir q3/u2-19.user.sols;./gjsolver q3/u2-19.user q3/u2-19.user.sols 1> q3/u2-19.user.out 2> q3/u2-19.user.err 

508:
	mkdir q3/u2-2.user.sols;./gjsolver q3/u2-2.user q3/u2-2.user.sols 1> q3/u2-2.user.out 2> q3/u2-2.user.err 

509:
	mkdir q3/u2-20.user.sols;./gjsolver q3/u2-20.user q3/u2-20.user.sols 1> q3/u2-20.user.out 2> q3/u2-20.user.err 

510:
	mkdir q3/u2-21.user.sols;./gjsolver q3/u2-21.user q3/u2-21.user.sols 1> q3/u2-21.user.out 2> q3/u2-21.user.err 

511:
	mkdir q3/u2-22.user.sols;./gjsolver q3/u2-22.user q3/u2-22.user.sols 1> q3/u2-22.user.out 2> q3/u2-22.user.err 

512:
	mkdir q3/u2-23.user.sols;./gjsolver q3/u2-23.user q3/u2-23.user.sols 1> q3/u2-23.user.out 2> q3/u2-23.user.err 

513:
	mkdir q3/u2-24.user.sols;./gjsolver q3/u2-24.user q3/u2-24.user.sols 1> q3/u2-24.user.out 2> q3/u2-24.user.err 

514:
	mkdir q3/u2-25.user.sols;./gjsolver q3/u2-25.user q3/u2-25.user.sols 1> q3/u2-25.user.out 2> q3/u2-25.user.err 

515:
	mkdir q3/u2-26.user.sols;./gjsolver q3/u2-26.user q3/u2-26.user.sols 1> q3/u2-26.user.out 2> q3/u2-26.user.err 

516:
	mkdir q3/u2-27.user.sols;./gjsolver q3/u2-27.user q3/u2-27.user.sols 1> q3/u2-27.user.out 2> q3/u2-27.user.err 

517:
	mkdir q3/u2-28.user.sols;./gjsolver q3/u2-28.user q3/u2-28.user.sols 1> q3/u2-28.user.out 2> q3/u2-28.user.err 

518:
	mkdir q3/u2-29.user.sols;./gjsolver q3/u2-29.user q3/u2-29.user.sols 1> q3/u2-29.user.out 2> q3/u2-29.user.err 

519:
	mkdir q3/u2-3.user.sols;./gjsolver q3/u2-3.user q3/u2-3.user.sols 1> q3/u2-3.user.out 2> q3/u2-3.user.err 

520:
	mkdir q3/u2-30.user.sols;./gjsolver q3/u2-30.user q3/u2-30.user.sols 1> q3/u2-30.user.out 2> q3/u2-30.user.err 

521:
	mkdir q3/u2-4.user.sols;./gjsolver q3/u2-4.user q3/u2-4.user.sols 1> q3/u2-4.user.out 2> q3/u2-4.user.err 

522:
	mkdir q3/u2-5.user.sols;./gjsolver q3/u2-5.user q3/u2-5.user.sols 1> q3/u2-5.user.out 2> q3/u2-5.user.err 

523:
	mkdir q3/u2-6.user.sols;./gjsolver q3/u2-6.user q3/u2-6.user.sols 1> q3/u2-6.user.out 2> q3/u2-6.user.err 

524:
	mkdir q3/u2-7.user.sols;./gjsolver q3/u2-7.user q3/u2-7.user.sols 1> q3/u2-7.user.out 2> q3/u2-7.user.err 

525:
	mkdir q3/u2-8.user.sols;./gjsolver q3/u2-8.user q3/u2-8.user.sols 1> q3/u2-8.user.out 2> q3/u2-8.user.err 

526:
	mkdir q3/u2-9.user.sols;./gjsolver q3/u2-9.user q3/u2-9.user.sols 1> q3/u2-9.user.out 2> q3/u2-9.user.err 

527:
	mkdir q3/u3-1.user.sols;./gjsolver q3/u3-1.user q3/u3-1.user.sols 1> q3/u3-1.user.out 2> q3/u3-1.user.err 

528:
	mkdir q3/u3-10.user.sols;./gjsolver q3/u3-10.user q3/u3-10.user.sols 1> q3/u3-10.user.out 2> q3/u3-10.user.err 

529:
	mkdir q3/u3-11.user.sols;./gjsolver q3/u3-11.user q3/u3-11.user.sols 1> q3/u3-11.user.out 2> q3/u3-11.user.err 

530:
	mkdir q3/u3-12.user.sols;./gjsolver q3/u3-12.user q3/u3-12.user.sols 1> q3/u3-12.user.out 2> q3/u3-12.user.err 

531:
	mkdir q3/u3-13.user.sols;./gjsolver q3/u3-13.user q3/u3-13.user.sols 1> q3/u3-13.user.out 2> q3/u3-13.user.err 

532:
	mkdir q3/u3-14.user.sols;./gjsolver q3/u3-14.user q3/u3-14.user.sols 1> q3/u3-14.user.out 2> q3/u3-14.user.err 

533:
	mkdir q3/u3-15.user.sols;./gjsolver q3/u3-15.user q3/u3-15.user.sols 1> q3/u3-15.user.out 2> q3/u3-15.user.err 

534:
	mkdir q3/u3-16.user.sols;./gjsolver q3/u3-16.user q3/u3-16.user.sols 1> q3/u3-16.user.out 2> q3/u3-16.user.err 

535:
	mkdir q3/u3-17.user.sols;./gjsolver q3/u3-17.user q3/u3-17.user.sols 1> q3/u3-17.user.out 2> q3/u3-17.user.err 

536:
	mkdir q3/u3-18.user.sols;./gjsolver q3/u3-18.user q3/u3-18.user.sols 1> q3/u3-18.user.out 2> q3/u3-18.user.err 

537:
	mkdir q3/u3-19.user.sols;./gjsolver q3/u3-19.user q3/u3-19.user.sols 1> q3/u3-19.user.out 2> q3/u3-19.user.err 

538:
	mkdir q3/u3-2.user.sols;./gjsolver q3/u3-2.user q3/u3-2.user.sols 1> q3/u3-2.user.out 2> q3/u3-2.user.err 

539:
	mkdir q3/u3-20.user.sols;./gjsolver q3/u3-20.user q3/u3-20.user.sols 1> q3/u3-20.user.out 2> q3/u3-20.user.err 

540:
	mkdir q3/u3-21.user.sols;./gjsolver q3/u3-21.user q3/u3-21.user.sols 1> q3/u3-21.user.out 2> q3/u3-21.user.err 

541:
	mkdir q3/u3-22.user.sols;./gjsolver q3/u3-22.user q3/u3-22.user.sols 1> q3/u3-22.user.out 2> q3/u3-22.user.err 

542:
	mkdir q3/u3-23.user.sols;./gjsolver q3/u3-23.user q3/u3-23.user.sols 1> q3/u3-23.user.out 2> q3/u3-23.user.err 

543:
	mkdir q3/u3-24.user.sols;./gjsolver q3/u3-24.user q3/u3-24.user.sols 1> q3/u3-24.user.out 2> q3/u3-24.user.err 

544:
	mkdir q3/u3-25.user.sols;./gjsolver q3/u3-25.user q3/u3-25.user.sols 1> q3/u3-25.user.out 2> q3/u3-25.user.err 

545:
	mkdir q3/u3-26.user.sols;./gjsolver q3/u3-26.user q3/u3-26.user.sols 1> q3/u3-26.user.out 2> q3/u3-26.user.err 

546:
	mkdir q3/u3-27.user.sols;./gjsolver q3/u3-27.user q3/u3-27.user.sols 1> q3/u3-27.user.out 2> q3/u3-27.user.err 

547:
	mkdir q3/u3-28.user.sols;./gjsolver q3/u3-28.user q3/u3-28.user.sols 1> q3/u3-28.user.out 2> q3/u3-28.user.err 

548:
	mkdir q3/u3-29.user.sols;./gjsolver q3/u3-29.user q3/u3-29.user.sols 1> q3/u3-29.user.out 2> q3/u3-29.user.err 

549:
	mkdir q3/u3-3.user.sols;./gjsolver q3/u3-3.user q3/u3-3.user.sols 1> q3/u3-3.user.out 2> q3/u3-3.user.err 

550:
	mkdir q3/u3-30.user.sols;./gjsolver q3/u3-30.user q3/u3-30.user.sols 1> q3/u3-30.user.out 2> q3/u3-30.user.err 

551:
	mkdir q3/u3-4.user.sols;./gjsolver q3/u3-4.user q3/u3-4.user.sols 1> q3/u3-4.user.out 2> q3/u3-4.user.err 

552:
	mkdir q3/u3-5.user.sols;./gjsolver q3/u3-5.user q3/u3-5.user.sols 1> q3/u3-5.user.out 2> q3/u3-5.user.err 

553:
	mkdir q3/u3-6.user.sols;./gjsolver q3/u3-6.user q3/u3-6.user.sols 1> q3/u3-6.user.out 2> q3/u3-6.user.err 

554:
	mkdir q3/u3-7.user.sols;./gjsolver q3/u3-7.user q3/u3-7.user.sols 1> q3/u3-7.user.out 2> q3/u3-7.user.err 

555:
	mkdir q3/u3-8.user.sols;./gjsolver q3/u3-8.user q3/u3-8.user.sols 1> q3/u3-8.user.out 2> q3/u3-8.user.err 

556:
	mkdir q3/u3-9.user.sols;./gjsolver q3/u3-9.user q3/u3-9.user.sols 1> q3/u3-9.user.out 2> q3/u3-9.user.err 

557:
	mkdir q3/u4-1.user.sols;./gjsolver q3/u4-1.user q3/u4-1.user.sols 1> q3/u4-1.user.out 2> q3/u4-1.user.err 

558:
	mkdir q3/u4-10.user.sols;./gjsolver q3/u4-10.user q3/u4-10.user.sols 1> q3/u4-10.user.out 2> q3/u4-10.user.err 

559:
	mkdir q3/u4-11.user.sols;./gjsolver q3/u4-11.user q3/u4-11.user.sols 1> q3/u4-11.user.out 2> q3/u4-11.user.err 

560:
	mkdir q3/u4-12.user.sols;./gjsolver q3/u4-12.user q3/u4-12.user.sols 1> q3/u4-12.user.out 2> q3/u4-12.user.err 

561:
	mkdir q3/u4-13.user.sols;./gjsolver q3/u4-13.user q3/u4-13.user.sols 1> q3/u4-13.user.out 2> q3/u4-13.user.err 

562:
	mkdir q3/u4-14.user.sols;./gjsolver q3/u4-14.user q3/u4-14.user.sols 1> q3/u4-14.user.out 2> q3/u4-14.user.err 

563:
	mkdir q3/u4-15.user.sols;./gjsolver q3/u4-15.user q3/u4-15.user.sols 1> q3/u4-15.user.out 2> q3/u4-15.user.err 

564:
	mkdir q3/u4-16.user.sols;./gjsolver q3/u4-16.user q3/u4-16.user.sols 1> q3/u4-16.user.out 2> q3/u4-16.user.err 

565:
	mkdir q3/u4-17.user.sols;./gjsolver q3/u4-17.user q3/u4-17.user.sols 1> q3/u4-17.user.out 2> q3/u4-17.user.err 

566:
	mkdir q3/u4-18.user.sols;./gjsolver q3/u4-18.user q3/u4-18.user.sols 1> q3/u4-18.user.out 2> q3/u4-18.user.err 

567:
	mkdir q3/u4-19.user.sols;./gjsolver q3/u4-19.user q3/u4-19.user.sols 1> q3/u4-19.user.out 2> q3/u4-19.user.err 

568:
	mkdir q3/u4-2.user.sols;./gjsolver q3/u4-2.user q3/u4-2.user.sols 1> q3/u4-2.user.out 2> q3/u4-2.user.err 

569:
	mkdir q3/u4-20.user.sols;./gjsolver q3/u4-20.user q3/u4-20.user.sols 1> q3/u4-20.user.out 2> q3/u4-20.user.err 

570:
	mkdir q3/u4-21.user.sols;./gjsolver q3/u4-21.user q3/u4-21.user.sols 1> q3/u4-21.user.out 2> q3/u4-21.user.err 

571:
	mkdir q3/u4-22.user.sols;./gjsolver q3/u4-22.user q3/u4-22.user.sols 1> q3/u4-22.user.out 2> q3/u4-22.user.err 

572:
	mkdir q3/u4-23.user.sols;./gjsolver q3/u4-23.user q3/u4-23.user.sols 1> q3/u4-23.user.out 2> q3/u4-23.user.err 

573:
	mkdir q3/u4-24.user.sols;./gjsolver q3/u4-24.user q3/u4-24.user.sols 1> q3/u4-24.user.out 2> q3/u4-24.user.err 

574:
	mkdir q3/u4-25.user.sols;./gjsolver q3/u4-25.user q3/u4-25.user.sols 1> q3/u4-25.user.out 2> q3/u4-25.user.err 

575:
	mkdir q3/u4-26.user.sols;./gjsolver q3/u4-26.user q3/u4-26.user.sols 1> q3/u4-26.user.out 2> q3/u4-26.user.err 

576:
	mkdir q3/u4-27.user.sols;./gjsolver q3/u4-27.user q3/u4-27.user.sols 1> q3/u4-27.user.out 2> q3/u4-27.user.err 

577:
	mkdir q3/u4-28.user.sols;./gjsolver q3/u4-28.user q3/u4-28.user.sols 1> q3/u4-28.user.out 2> q3/u4-28.user.err 

578:
	mkdir q3/u4-29.user.sols;./gjsolver q3/u4-29.user q3/u4-29.user.sols 1> q3/u4-29.user.out 2> q3/u4-29.user.err 

579:
	mkdir q3/u4-3.user.sols;./gjsolver q3/u4-3.user q3/u4-3.user.sols 1> q3/u4-3.user.out 2> q3/u4-3.user.err 

580:
	mkdir q3/u4-30.user.sols;./gjsolver q3/u4-30.user q3/u4-30.user.sols 1> q3/u4-30.user.out 2> q3/u4-30.user.err 

581:
	mkdir q3/u4-4.user.sols;./gjsolver q3/u4-4.user q3/u4-4.user.sols 1> q3/u4-4.user.out 2> q3/u4-4.user.err 

582:
	mkdir q3/u4-5.user.sols;./gjsolver q3/u4-5.user q3/u4-5.user.sols 1> q3/u4-5.user.out 2> q3/u4-5.user.err 

583:
	mkdir q3/u4-6.user.sols;./gjsolver q3/u4-6.user q3/u4-6.user.sols 1> q3/u4-6.user.out 2> q3/u4-6.user.err 

584:
	mkdir q3/u4-7.user.sols;./gjsolver q3/u4-7.user q3/u4-7.user.sols 1> q3/u4-7.user.out 2> q3/u4-7.user.err 

585:
	mkdir q3/u4-8.user.sols;./gjsolver q3/u4-8.user q3/u4-8.user.sols 1> q3/u4-8.user.out 2> q3/u4-8.user.err 

586:
	mkdir q3/u4-9.user.sols;./gjsolver q3/u4-9.user q3/u4-9.user.sols 1> q3/u4-9.user.out 2> q3/u4-9.user.err 

587:
	mkdir q3/u5-1.user.sols;./gjsolver q3/u5-1.user q3/u5-1.user.sols 1> q3/u5-1.user.out 2> q3/u5-1.user.err 

588:
	mkdir q3/u5-10.user.sols;./gjsolver q3/u5-10.user q3/u5-10.user.sols 1> q3/u5-10.user.out 2> q3/u5-10.user.err 

589:
	mkdir q3/u5-11.user.sols;./gjsolver q3/u5-11.user q3/u5-11.user.sols 1> q3/u5-11.user.out 2> q3/u5-11.user.err 

590:
	mkdir q3/u5-12.user.sols;./gjsolver q3/u5-12.user q3/u5-12.user.sols 1> q3/u5-12.user.out 2> q3/u5-12.user.err 

591:
	mkdir q3/u5-13.user.sols;./gjsolver q3/u5-13.user q3/u5-13.user.sols 1> q3/u5-13.user.out 2> q3/u5-13.user.err 

592:
	mkdir q3/u5-14.user.sols;./gjsolver q3/u5-14.user q3/u5-14.user.sols 1> q3/u5-14.user.out 2> q3/u5-14.user.err 

593:
	mkdir q3/u5-15.user.sols;./gjsolver q3/u5-15.user q3/u5-15.user.sols 1> q3/u5-15.user.out 2> q3/u5-15.user.err 

594:
	mkdir q3/u5-16.user.sols;./gjsolver q3/u5-16.user q3/u5-16.user.sols 1> q3/u5-16.user.out 2> q3/u5-16.user.err 

595:
	mkdir q3/u5-17.user.sols;./gjsolver q3/u5-17.user q3/u5-17.user.sols 1> q3/u5-17.user.out 2> q3/u5-17.user.err 

596:
	mkdir q3/u5-18.user.sols;./gjsolver q3/u5-18.user q3/u5-18.user.sols 1> q3/u5-18.user.out 2> q3/u5-18.user.err 

597:
	mkdir q3/u5-19.user.sols;./gjsolver q3/u5-19.user q3/u5-19.user.sols 1> q3/u5-19.user.out 2> q3/u5-19.user.err 

598:
	mkdir q3/u5-2.user.sols;./gjsolver q3/u5-2.user q3/u5-2.user.sols 1> q3/u5-2.user.out 2> q3/u5-2.user.err 

599:
	mkdir q3/u5-20.user.sols;./gjsolver q3/u5-20.user q3/u5-20.user.sols 1> q3/u5-20.user.out 2> q3/u5-20.user.err 

600:
	mkdir q3/u5-21.user.sols;./gjsolver q3/u5-21.user q3/u5-21.user.sols 1> q3/u5-21.user.out 2> q3/u5-21.user.err 

601:
	mkdir q3/u5-22.user.sols;./gjsolver q3/u5-22.user q3/u5-22.user.sols 1> q3/u5-22.user.out 2> q3/u5-22.user.err 

602:
	mkdir q3/u5-23.user.sols;./gjsolver q3/u5-23.user q3/u5-23.user.sols 1> q3/u5-23.user.out 2> q3/u5-23.user.err 

603:
	mkdir q3/u5-24.user.sols;./gjsolver q3/u5-24.user q3/u5-24.user.sols 1> q3/u5-24.user.out 2> q3/u5-24.user.err 

604:
	mkdir q3/u5-25.user.sols;./gjsolver q3/u5-25.user q3/u5-25.user.sols 1> q3/u5-25.user.out 2> q3/u5-25.user.err 

605:
	mkdir q3/u5-26.user.sols;./gjsolver q3/u5-26.user q3/u5-26.user.sols 1> q3/u5-26.user.out 2> q3/u5-26.user.err 

606:
	mkdir q3/u5-27.user.sols;./gjsolver q3/u5-27.user q3/u5-27.user.sols 1> q3/u5-27.user.out 2> q3/u5-27.user.err 

607:
	mkdir q3/u5-28.user.sols;./gjsolver q3/u5-28.user q3/u5-28.user.sols 1> q3/u5-28.user.out 2> q3/u5-28.user.err 

608:
	mkdir q3/u5-29.user.sols;./gjsolver q3/u5-29.user q3/u5-29.user.sols 1> q3/u5-29.user.out 2> q3/u5-29.user.err 

609:
	mkdir q3/u5-3.user.sols;./gjsolver q3/u5-3.user q3/u5-3.user.sols 1> q3/u5-3.user.out 2> q3/u5-3.user.err 

610:
	mkdir q3/u5-30.user.sols;./gjsolver q3/u5-30.user q3/u5-30.user.sols 1> q3/u5-30.user.out 2> q3/u5-30.user.err 

611:
	mkdir q3/u5-4.user.sols;./gjsolver q3/u5-4.user q3/u5-4.user.sols 1> q3/u5-4.user.out 2> q3/u5-4.user.err 

612:
	mkdir q3/u5-5.user.sols;./gjsolver q3/u5-5.user q3/u5-5.user.sols 1> q3/u5-5.user.out 2> q3/u5-5.user.err 

613:
	mkdir q3/u5-6.user.sols;./gjsolver q3/u5-6.user q3/u5-6.user.sols 1> q3/u5-6.user.out 2> q3/u5-6.user.err 

614:
	mkdir q3/u5-7.user.sols;./gjsolver q3/u5-7.user q3/u5-7.user.sols 1> q3/u5-7.user.out 2> q3/u5-7.user.err 

615:
	mkdir q3/u5-8.user.sols;./gjsolver q3/u5-8.user q3/u5-8.user.sols 1> q3/u5-8.user.out 2> q3/u5-8.user.err 

616:
	mkdir q3/u5-9.user.sols;./gjsolver q3/u5-9.user q3/u5-9.user.sols 1> q3/u5-9.user.out 2> q3/u5-9.user.err 

617:
	mkdir q4a/maxu0.1.1.user.sols;./gjsolver q4a/maxu0.1.1.user q4a/maxu0.1.1.user.sols 1> q4a/maxu0.1.1.user.out 2> q4a/maxu0.1.1.user.err 

618:
	mkdir q4a/maxu0.1.2.user.sols;./gjsolver q4a/maxu0.1.2.user q4a/maxu0.1.2.user.sols 1> q4a/maxu0.1.2.user.out 2> q4a/maxu0.1.2.user.err 

619:
	mkdir q4a/maxu0.1.3.user.sols;./gjsolver q4a/maxu0.1.3.user q4a/maxu0.1.3.user.sols 1> q4a/maxu0.1.3.user.out 2> q4a/maxu0.1.3.user.err 

620:
	mkdir q4a/maxu0.1.4.user.sols;./gjsolver q4a/maxu0.1.4.user q4a/maxu0.1.4.user.sols 1> q4a/maxu0.1.4.user.out 2> q4a/maxu0.1.4.user.err 

621:
	mkdir q4a/maxu0.1.5.user.sols;./gjsolver q4a/maxu0.1.5.user q4a/maxu0.1.5.user.sols 1> q4a/maxu0.1.5.user.out 2> q4a/maxu0.1.5.user.err 

622:
	mkdir q4a/maxu0.2.1.user.sols;./gjsolver q4a/maxu0.2.1.user q4a/maxu0.2.1.user.sols 1> q4a/maxu0.2.1.user.out 2> q4a/maxu0.2.1.user.err 

623:
	mkdir q4a/maxu0.2.2.user.sols;./gjsolver q4a/maxu0.2.2.user q4a/maxu0.2.2.user.sols 1> q4a/maxu0.2.2.user.out 2> q4a/maxu0.2.2.user.err 

624:
	mkdir q4a/maxu0.2.3.user.sols;./gjsolver q4a/maxu0.2.3.user q4a/maxu0.2.3.user.sols 1> q4a/maxu0.2.3.user.out 2> q4a/maxu0.2.3.user.err 

625:
	mkdir q4a/maxu0.2.4.user.sols;./gjsolver q4a/maxu0.2.4.user q4a/maxu0.2.4.user.sols 1> q4a/maxu0.2.4.user.out 2> q4a/maxu0.2.4.user.err 

626:
	mkdir q4a/maxu0.2.5.user.sols;./gjsolver q4a/maxu0.2.5.user q4a/maxu0.2.5.user.sols 1> q4a/maxu0.2.5.user.out 2> q4a/maxu0.2.5.user.err 

627:
	mkdir q4a/maxu0.3.1.user.sols;./gjsolver q4a/maxu0.3.1.user q4a/maxu0.3.1.user.sols 1> q4a/maxu0.3.1.user.out 2> q4a/maxu0.3.1.user.err 

628:
	mkdir q4a/maxu0.3.2.user.sols;./gjsolver q4a/maxu0.3.2.user q4a/maxu0.3.2.user.sols 1> q4a/maxu0.3.2.user.out 2> q4a/maxu0.3.2.user.err 

629:
	mkdir q4a/maxu0.3.3.user.sols;./gjsolver q4a/maxu0.3.3.user q4a/maxu0.3.3.user.sols 1> q4a/maxu0.3.3.user.out 2> q4a/maxu0.3.3.user.err 

630:
	mkdir q4a/maxu0.3.4.user.sols;./gjsolver q4a/maxu0.3.4.user q4a/maxu0.3.4.user.sols 1> q4a/maxu0.3.4.user.out 2> q4a/maxu0.3.4.user.err 

631:
	mkdir q4a/maxu0.3.5.user.sols;./gjsolver q4a/maxu0.3.5.user q4a/maxu0.3.5.user.sols 1> q4a/maxu0.3.5.user.out 2> q4a/maxu0.3.5.user.err 

632:
	mkdir q4a/maxu0.4.1.user.sols;./gjsolver q4a/maxu0.4.1.user q4a/maxu0.4.1.user.sols 1> q4a/maxu0.4.1.user.out 2> q4a/maxu0.4.1.user.err 

633:
	mkdir q4a/maxu0.4.2.user.sols;./gjsolver q4a/maxu0.4.2.user q4a/maxu0.4.2.user.sols 1> q4a/maxu0.4.2.user.out 2> q4a/maxu0.4.2.user.err 

634:
	mkdir q4a/maxu0.4.3.user.sols;./gjsolver q4a/maxu0.4.3.user q4a/maxu0.4.3.user.sols 1> q4a/maxu0.4.3.user.out 2> q4a/maxu0.4.3.user.err 

635:
	mkdir q4a/maxu0.4.4.user.sols;./gjsolver q4a/maxu0.4.4.user q4a/maxu0.4.4.user.sols 1> q4a/maxu0.4.4.user.out 2> q4a/maxu0.4.4.user.err 

636:
	mkdir q4a/maxu0.4.5.user.sols;./gjsolver q4a/maxu0.4.5.user q4a/maxu0.4.5.user.sols 1> q4a/maxu0.4.5.user.out 2> q4a/maxu0.4.5.user.err 

637:
	mkdir q4a/maxu0.5.1.user.sols;./gjsolver q4a/maxu0.5.1.user q4a/maxu0.5.1.user.sols 1> q4a/maxu0.5.1.user.out 2> q4a/maxu0.5.1.user.err 

638:
	mkdir q4a/maxu0.5.2.user.sols;./gjsolver q4a/maxu0.5.2.user q4a/maxu0.5.2.user.sols 1> q4a/maxu0.5.2.user.out 2> q4a/maxu0.5.2.user.err 

639:
	mkdir q4a/maxu0.5.3.user.sols;./gjsolver q4a/maxu0.5.3.user q4a/maxu0.5.3.user.sols 1> q4a/maxu0.5.3.user.out 2> q4a/maxu0.5.3.user.err 

640:
	mkdir q4a/maxu0.5.4.user.sols;./gjsolver q4a/maxu0.5.4.user q4a/maxu0.5.4.user.sols 1> q4a/maxu0.5.4.user.out 2> q4a/maxu0.5.4.user.err 

641:
	mkdir q4a/maxu0.5.5.user.sols;./gjsolver q4a/maxu0.5.5.user q4a/maxu0.5.5.user.sols 1> q4a/maxu0.5.5.user.out 2> q4a/maxu0.5.5.user.err 

642:
	mkdir q4a/maxu0.6.1.user.sols;./gjsolver q4a/maxu0.6.1.user q4a/maxu0.6.1.user.sols 1> q4a/maxu0.6.1.user.out 2> q4a/maxu0.6.1.user.err 

643:
	mkdir q4a/maxu0.6.2.user.sols;./gjsolver q4a/maxu0.6.2.user q4a/maxu0.6.2.user.sols 1> q4a/maxu0.6.2.user.out 2> q4a/maxu0.6.2.user.err 

644:
	mkdir q4a/maxu0.6.3.user.sols;./gjsolver q4a/maxu0.6.3.user q4a/maxu0.6.3.user.sols 1> q4a/maxu0.6.3.user.out 2> q4a/maxu0.6.3.user.err 

645:
	mkdir q4a/maxu0.6.4.user.sols;./gjsolver q4a/maxu0.6.4.user q4a/maxu0.6.4.user.sols 1> q4a/maxu0.6.4.user.out 2> q4a/maxu0.6.4.user.err 

646:
	mkdir q4a/maxu0.6.5.user.sols;./gjsolver q4a/maxu0.6.5.user q4a/maxu0.6.5.user.sols 1> q4a/maxu0.6.5.user.out 2> q4a/maxu0.6.5.user.err 

647:
	mkdir q4a/maxu0.7.1.user.sols;./gjsolver q4a/maxu0.7.1.user q4a/maxu0.7.1.user.sols 1> q4a/maxu0.7.1.user.out 2> q4a/maxu0.7.1.user.err 

648:
	mkdir q4a/maxu0.7.2.user.sols;./gjsolver q4a/maxu0.7.2.user q4a/maxu0.7.2.user.sols 1> q4a/maxu0.7.2.user.out 2> q4a/maxu0.7.2.user.err 

649:
	mkdir q4a/maxu0.7.3.user.sols;./gjsolver q4a/maxu0.7.3.user q4a/maxu0.7.3.user.sols 1> q4a/maxu0.7.3.user.out 2> q4a/maxu0.7.3.user.err 

650:
	mkdir q4a/maxu0.7.4.user.sols;./gjsolver q4a/maxu0.7.4.user q4a/maxu0.7.4.user.sols 1> q4a/maxu0.7.4.user.out 2> q4a/maxu0.7.4.user.err 

651:
	mkdir q4a/maxu0.7.5.user.sols;./gjsolver q4a/maxu0.7.5.user q4a/maxu0.7.5.user.sols 1> q4a/maxu0.7.5.user.out 2> q4a/maxu0.7.5.user.err 

652:
	mkdir q4a/maxu0.8.1.user.sols;./gjsolver q4a/maxu0.8.1.user q4a/maxu0.8.1.user.sols 1> q4a/maxu0.8.1.user.out 2> q4a/maxu0.8.1.user.err 

653:
	mkdir q4a/maxu0.8.2.user.sols;./gjsolver q4a/maxu0.8.2.user q4a/maxu0.8.2.user.sols 1> q4a/maxu0.8.2.user.out 2> q4a/maxu0.8.2.user.err 

654:
	mkdir q4a/maxu0.8.3.user.sols;./gjsolver q4a/maxu0.8.3.user q4a/maxu0.8.3.user.sols 1> q4a/maxu0.8.3.user.out 2> q4a/maxu0.8.3.user.err 

655:
	mkdir q4a/maxu0.8.4.user.sols;./gjsolver q4a/maxu0.8.4.user q4a/maxu0.8.4.user.sols 1> q4a/maxu0.8.4.user.out 2> q4a/maxu0.8.4.user.err 

656:
	mkdir q4a/maxu0.8.5.user.sols;./gjsolver q4a/maxu0.8.5.user q4a/maxu0.8.5.user.sols 1> q4a/maxu0.8.5.user.out 2> q4a/maxu0.8.5.user.err 

657:
	mkdir q4a/maxu0.9.1.user.sols;./gjsolver q4a/maxu0.9.1.user q4a/maxu0.9.1.user.sols 1> q4a/maxu0.9.1.user.out 2> q4a/maxu0.9.1.user.err 

658:
	mkdir q4a/maxu0.9.2.user.sols;./gjsolver q4a/maxu0.9.2.user q4a/maxu0.9.2.user.sols 1> q4a/maxu0.9.2.user.out 2> q4a/maxu0.9.2.user.err 

659:
	mkdir q4a/maxu0.9.3.user.sols;./gjsolver q4a/maxu0.9.3.user q4a/maxu0.9.3.user.sols 1> q4a/maxu0.9.3.user.out 2> q4a/maxu0.9.3.user.err 

660:
	mkdir q4a/maxu0.9.4.user.sols;./gjsolver q4a/maxu0.9.4.user q4a/maxu0.9.4.user.sols 1> q4a/maxu0.9.4.user.out 2> q4a/maxu0.9.4.user.err 

661:
	mkdir q4a/maxu0.9.5.user.sols;./gjsolver q4a/maxu0.9.5.user q4a/maxu0.9.5.user.sols 1> q4a/maxu0.9.5.user.out 2> q4a/maxu0.9.5.user.err 

662:
	mkdir q4a/maxu1.0.1.user.sols;./gjsolver q4a/maxu1.0.1.user q4a/maxu1.0.1.user.sols 1> q4a/maxu1.0.1.user.out 2> q4a/maxu1.0.1.user.err 

663:
	mkdir q4a/maxu1.0.2.user.sols;./gjsolver q4a/maxu1.0.2.user q4a/maxu1.0.2.user.sols 1> q4a/maxu1.0.2.user.out 2> q4a/maxu1.0.2.user.err 

664:
	mkdir q4a/maxu1.0.3.user.sols;./gjsolver q4a/maxu1.0.3.user q4a/maxu1.0.3.user.sols 1> q4a/maxu1.0.3.user.out 2> q4a/maxu1.0.3.user.err 

665:
	mkdir q4a/maxu1.0.4.user.sols;./gjsolver q4a/maxu1.0.4.user q4a/maxu1.0.4.user.sols 1> q4a/maxu1.0.4.user.out 2> q4a/maxu1.0.4.user.err 

666:
	mkdir q4a/maxu1.0.5.user.sols;./gjsolver q4a/maxu1.0.5.user q4a/maxu1.0.5.user.sols 1> q4a/maxu1.0.5.user.out 2> q4a/maxu1.0.5.user.err 

667:
	mkdir q4a/modu0.1.1.user.sols;./gjsolver q4a/modu0.1.1.user q4a/modu0.1.1.user.sols 1> q4a/modu0.1.1.user.out 2> q4a/modu0.1.1.user.err 

668:
	mkdir q4a/modu0.1.2.user.sols;./gjsolver q4a/modu0.1.2.user q4a/modu0.1.2.user.sols 1> q4a/modu0.1.2.user.out 2> q4a/modu0.1.2.user.err 

669:
	mkdir q4a/modu0.1.3.user.sols;./gjsolver q4a/modu0.1.3.user q4a/modu0.1.3.user.sols 1> q4a/modu0.1.3.user.out 2> q4a/modu0.1.3.user.err 

670:
	mkdir q4a/modu0.1.4.user.sols;./gjsolver q4a/modu0.1.4.user q4a/modu0.1.4.user.sols 1> q4a/modu0.1.4.user.out 2> q4a/modu0.1.4.user.err 

671:
	mkdir q4a/modu0.1.5.user.sols;./gjsolver q4a/modu0.1.5.user q4a/modu0.1.5.user.sols 1> q4a/modu0.1.5.user.out 2> q4a/modu0.1.5.user.err 

672:
	mkdir q4a/modu0.2.1.user.sols;./gjsolver q4a/modu0.2.1.user q4a/modu0.2.1.user.sols 1> q4a/modu0.2.1.user.out 2> q4a/modu0.2.1.user.err 

673:
	mkdir q4a/modu0.2.2.user.sols;./gjsolver q4a/modu0.2.2.user q4a/modu0.2.2.user.sols 1> q4a/modu0.2.2.user.out 2> q4a/modu0.2.2.user.err 

674:
	mkdir q4a/modu0.2.3.user.sols;./gjsolver q4a/modu0.2.3.user q4a/modu0.2.3.user.sols 1> q4a/modu0.2.3.user.out 2> q4a/modu0.2.3.user.err 

675:
	mkdir q4a/modu0.2.4.user.sols;./gjsolver q4a/modu0.2.4.user q4a/modu0.2.4.user.sols 1> q4a/modu0.2.4.user.out 2> q4a/modu0.2.4.user.err 

676:
	mkdir q4a/modu0.2.5.user.sols;./gjsolver q4a/modu0.2.5.user q4a/modu0.2.5.user.sols 1> q4a/modu0.2.5.user.out 2> q4a/modu0.2.5.user.err 

677:
	mkdir q4a/modu0.3.1.user.sols;./gjsolver q4a/modu0.3.1.user q4a/modu0.3.1.user.sols 1> q4a/modu0.3.1.user.out 2> q4a/modu0.3.1.user.err 

678:
	mkdir q4a/modu0.3.2.user.sols;./gjsolver q4a/modu0.3.2.user q4a/modu0.3.2.user.sols 1> q4a/modu0.3.2.user.out 2> q4a/modu0.3.2.user.err 

679:
	mkdir q4a/modu0.3.3.user.sols;./gjsolver q4a/modu0.3.3.user q4a/modu0.3.3.user.sols 1> q4a/modu0.3.3.user.out 2> q4a/modu0.3.3.user.err 

680:
	mkdir q4a/modu0.3.4.user.sols;./gjsolver q4a/modu0.3.4.user q4a/modu0.3.4.user.sols 1> q4a/modu0.3.4.user.out 2> q4a/modu0.3.4.user.err 

681:
	mkdir q4a/modu0.3.5.user.sols;./gjsolver q4a/modu0.3.5.user q4a/modu0.3.5.user.sols 1> q4a/modu0.3.5.user.out 2> q4a/modu0.3.5.user.err 

682:
	mkdir q4a/modu0.4.1.user.sols;./gjsolver q4a/modu0.4.1.user q4a/modu0.4.1.user.sols 1> q4a/modu0.4.1.user.out 2> q4a/modu0.4.1.user.err 

683:
	mkdir q4a/modu0.4.2.user.sols;./gjsolver q4a/modu0.4.2.user q4a/modu0.4.2.user.sols 1> q4a/modu0.4.2.user.out 2> q4a/modu0.4.2.user.err 

684:
	mkdir q4a/modu0.4.3.user.sols;./gjsolver q4a/modu0.4.3.user q4a/modu0.4.3.user.sols 1> q4a/modu0.4.3.user.out 2> q4a/modu0.4.3.user.err 

685:
	mkdir q4a/modu0.4.4.user.sols;./gjsolver q4a/modu0.4.4.user q4a/modu0.4.4.user.sols 1> q4a/modu0.4.4.user.out 2> q4a/modu0.4.4.user.err 

686:
	mkdir q4a/modu0.4.5.user.sols;./gjsolver q4a/modu0.4.5.user q4a/modu0.4.5.user.sols 1> q4a/modu0.4.5.user.out 2> q4a/modu0.4.5.user.err 

687:
	mkdir q4a/modu0.5.1.user.sols;./gjsolver q4a/modu0.5.1.user q4a/modu0.5.1.user.sols 1> q4a/modu0.5.1.user.out 2> q4a/modu0.5.1.user.err 

688:
	mkdir q4a/modu0.5.2.user.sols;./gjsolver q4a/modu0.5.2.user q4a/modu0.5.2.user.sols 1> q4a/modu0.5.2.user.out 2> q4a/modu0.5.2.user.err 

689:
	mkdir q4a/modu0.5.3.user.sols;./gjsolver q4a/modu0.5.3.user q4a/modu0.5.3.user.sols 1> q4a/modu0.5.3.user.out 2> q4a/modu0.5.3.user.err 

690:
	mkdir q4a/modu0.5.4.user.sols;./gjsolver q4a/modu0.5.4.user q4a/modu0.5.4.user.sols 1> q4a/modu0.5.4.user.out 2> q4a/modu0.5.4.user.err 

691:
	mkdir q4a/modu0.5.5.user.sols;./gjsolver q4a/modu0.5.5.user q4a/modu0.5.5.user.sols 1> q4a/modu0.5.5.user.out 2> q4a/modu0.5.5.user.err 

692:
	mkdir q4a/modu0.6.1.user.sols;./gjsolver q4a/modu0.6.1.user q4a/modu0.6.1.user.sols 1> q4a/modu0.6.1.user.out 2> q4a/modu0.6.1.user.err 

693:
	mkdir q4a/modu0.6.2.user.sols;./gjsolver q4a/modu0.6.2.user q4a/modu0.6.2.user.sols 1> q4a/modu0.6.2.user.out 2> q4a/modu0.6.2.user.err 

694:
	mkdir q4a/modu0.6.3.user.sols;./gjsolver q4a/modu0.6.3.user q4a/modu0.6.3.user.sols 1> q4a/modu0.6.3.user.out 2> q4a/modu0.6.3.user.err 

695:
	mkdir q4a/modu0.6.4.user.sols;./gjsolver q4a/modu0.6.4.user q4a/modu0.6.4.user.sols 1> q4a/modu0.6.4.user.out 2> q4a/modu0.6.4.user.err 

696:
	mkdir q4a/modu0.6.5.user.sols;./gjsolver q4a/modu0.6.5.user q4a/modu0.6.5.user.sols 1> q4a/modu0.6.5.user.out 2> q4a/modu0.6.5.user.err 

697:
	mkdir q4a/modu0.7.1.user.sols;./gjsolver q4a/modu0.7.1.user q4a/modu0.7.1.user.sols 1> q4a/modu0.7.1.user.out 2> q4a/modu0.7.1.user.err 

698:
	mkdir q4a/modu0.7.2.user.sols;./gjsolver q4a/modu0.7.2.user q4a/modu0.7.2.user.sols 1> q4a/modu0.7.2.user.out 2> q4a/modu0.7.2.user.err 

699:
	mkdir q4a/modu0.7.3.user.sols;./gjsolver q4a/modu0.7.3.user q4a/modu0.7.3.user.sols 1> q4a/modu0.7.3.user.out 2> q4a/modu0.7.3.user.err 

700:
	mkdir q4a/modu0.7.4.user.sols;./gjsolver q4a/modu0.7.4.user q4a/modu0.7.4.user.sols 1> q4a/modu0.7.4.user.out 2> q4a/modu0.7.4.user.err 

701:
	mkdir q4a/modu0.7.5.user.sols;./gjsolver q4a/modu0.7.5.user q4a/modu0.7.5.user.sols 1> q4a/modu0.7.5.user.out 2> q4a/modu0.7.5.user.err 

702:
	mkdir q4a/modu0.8.1.user.sols;./gjsolver q4a/modu0.8.1.user q4a/modu0.8.1.user.sols 1> q4a/modu0.8.1.user.out 2> q4a/modu0.8.1.user.err 

703:
	mkdir q4a/modu0.8.2.user.sols;./gjsolver q4a/modu0.8.2.user q4a/modu0.8.2.user.sols 1> q4a/modu0.8.2.user.out 2> q4a/modu0.8.2.user.err 

704:
	mkdir q4a/modu0.8.3.user.sols;./gjsolver q4a/modu0.8.3.user q4a/modu0.8.3.user.sols 1> q4a/modu0.8.3.user.out 2> q4a/modu0.8.3.user.err 

705:
	mkdir q4a/modu0.8.4.user.sols;./gjsolver q4a/modu0.8.4.user q4a/modu0.8.4.user.sols 1> q4a/modu0.8.4.user.out 2> q4a/modu0.8.4.user.err 

706:
	mkdir q4a/modu0.8.5.user.sols;./gjsolver q4a/modu0.8.5.user q4a/modu0.8.5.user.sols 1> q4a/modu0.8.5.user.out 2> q4a/modu0.8.5.user.err 

707:
	mkdir q4a/modu0.9.1.user.sols;./gjsolver q4a/modu0.9.1.user q4a/modu0.9.1.user.sols 1> q4a/modu0.9.1.user.out 2> q4a/modu0.9.1.user.err 

708:
	mkdir q4a/modu0.9.2.user.sols;./gjsolver q4a/modu0.9.2.user q4a/modu0.9.2.user.sols 1> q4a/modu0.9.2.user.out 2> q4a/modu0.9.2.user.err 

709:
	mkdir q4a/modu0.9.3.user.sols;./gjsolver q4a/modu0.9.3.user q4a/modu0.9.3.user.sols 1> q4a/modu0.9.3.user.out 2> q4a/modu0.9.3.user.err 

710:
	mkdir q4a/modu0.9.4.user.sols;./gjsolver q4a/modu0.9.4.user q4a/modu0.9.4.user.sols 1> q4a/modu0.9.4.user.out 2> q4a/modu0.9.4.user.err 

711:
	mkdir q4a/modu0.9.5.user.sols;./gjsolver q4a/modu0.9.5.user q4a/modu0.9.5.user.sols 1> q4a/modu0.9.5.user.out 2> q4a/modu0.9.5.user.err 

712:
	mkdir q4a/modu1.0.1.user.sols;./gjsolver q4a/modu1.0.1.user q4a/modu1.0.1.user.sols 1> q4a/modu1.0.1.user.out 2> q4a/modu1.0.1.user.err 

713:
	mkdir q4a/modu1.0.2.user.sols;./gjsolver q4a/modu1.0.2.user q4a/modu1.0.2.user.sols 1> q4a/modu1.0.2.user.out 2> q4a/modu1.0.2.user.err 

714:
	mkdir q4a/modu1.0.3.user.sols;./gjsolver q4a/modu1.0.3.user q4a/modu1.0.3.user.sols 1> q4a/modu1.0.3.user.out 2> q4a/modu1.0.3.user.err 

715:
	mkdir q4a/modu1.0.4.user.sols;./gjsolver q4a/modu1.0.4.user q4a/modu1.0.4.user.sols 1> q4a/modu1.0.4.user.out 2> q4a/modu1.0.4.user.err 

716:
	mkdir q4a/modu1.0.5.user.sols;./gjsolver q4a/modu1.0.5.user q4a/modu1.0.5.user.sols 1> q4a/modu1.0.5.user.out 2> q4a/modu1.0.5.user.err 

717:
	mkdir q5a/alwaysupdate.1209600.user.sols;./gjsolver q5a/alwaysupdate.1209600.user q5a/alwaysupdate.1209600.user.sols 1> q5a/alwaysupdate.1209600.user.out 2> q5a/alwaysupdate.1209600.user.err 

718:
	mkdir q5a/alwaysupdate.1814400.user.sols;./gjsolver q5a/alwaysupdate.1814400.user q5a/alwaysupdate.1814400.user.sols 1> q5a/alwaysupdate.1814400.user.out 2> q5a/alwaysupdate.1814400.user.err 

719:
	mkdir q5a/alwaysupdate.2419200.user.sols;./gjsolver q5a/alwaysupdate.2419200.user q5a/alwaysupdate.2419200.user.sols 1> q5a/alwaysupdate.2419200.user.out 2> q5a/alwaysupdate.2419200.user.err 

720:
	mkdir q5a/alwaysupdate.604800.user.sols;./gjsolver q5a/alwaysupdate.604800.user q5a/alwaysupdate.604800.user.sols 1> q5a/alwaysupdate.604800.user.out 2> q5a/alwaysupdate.604800.user.err 

721:
	mkdir q5a/u1209600.0.2.1.user.sols;./gjsolver q5a/u1209600.0.2.1.user q5a/u1209600.0.2.1.user.sols 1> q5a/u1209600.0.2.1.user.out 2> q5a/u1209600.0.2.1.user.err 

722:
	mkdir q5a/u1209600.0.2.2.user.sols;./gjsolver q5a/u1209600.0.2.2.user q5a/u1209600.0.2.2.user.sols 1> q5a/u1209600.0.2.2.user.out 2> q5a/u1209600.0.2.2.user.err 

723:
	mkdir q5a/u1209600.0.2.3.user.sols;./gjsolver q5a/u1209600.0.2.3.user q5a/u1209600.0.2.3.user.sols 1> q5a/u1209600.0.2.3.user.out 2> q5a/u1209600.0.2.3.user.err 

724:
	mkdir q5a/u1209600.0.2.4.user.sols;./gjsolver q5a/u1209600.0.2.4.user q5a/u1209600.0.2.4.user.sols 1> q5a/u1209600.0.2.4.user.out 2> q5a/u1209600.0.2.4.user.err 

725:
	mkdir q5a/u1209600.0.2.5.user.sols;./gjsolver q5a/u1209600.0.2.5.user q5a/u1209600.0.2.5.user.sols 1> q5a/u1209600.0.2.5.user.out 2> q5a/u1209600.0.2.5.user.err 

726:
	mkdir q5a/u1209600.0.4.1.user.sols;./gjsolver q5a/u1209600.0.4.1.user q5a/u1209600.0.4.1.user.sols 1> q5a/u1209600.0.4.1.user.out 2> q5a/u1209600.0.4.1.user.err 

727:
	mkdir q5a/u1209600.0.4.2.user.sols;./gjsolver q5a/u1209600.0.4.2.user q5a/u1209600.0.4.2.user.sols 1> q5a/u1209600.0.4.2.user.out 2> q5a/u1209600.0.4.2.user.err 

728:
	mkdir q5a/u1209600.0.4.3.user.sols;./gjsolver q5a/u1209600.0.4.3.user q5a/u1209600.0.4.3.user.sols 1> q5a/u1209600.0.4.3.user.out 2> q5a/u1209600.0.4.3.user.err 

729:
	mkdir q5a/u1209600.0.4.4.user.sols;./gjsolver q5a/u1209600.0.4.4.user q5a/u1209600.0.4.4.user.sols 1> q5a/u1209600.0.4.4.user.out 2> q5a/u1209600.0.4.4.user.err 

730:
	mkdir q5a/u1209600.0.4.5.user.sols;./gjsolver q5a/u1209600.0.4.5.user q5a/u1209600.0.4.5.user.sols 1> q5a/u1209600.0.4.5.user.out 2> q5a/u1209600.0.4.5.user.err 

731:
	mkdir q5a/u1209600.0.6.1.user.sols;./gjsolver q5a/u1209600.0.6.1.user q5a/u1209600.0.6.1.user.sols 1> q5a/u1209600.0.6.1.user.out 2> q5a/u1209600.0.6.1.user.err 

732:
	mkdir q5a/u1209600.0.6.2.user.sols;./gjsolver q5a/u1209600.0.6.2.user q5a/u1209600.0.6.2.user.sols 1> q5a/u1209600.0.6.2.user.out 2> q5a/u1209600.0.6.2.user.err 

733:
	mkdir q5a/u1209600.0.6.3.user.sols;./gjsolver q5a/u1209600.0.6.3.user q5a/u1209600.0.6.3.user.sols 1> q5a/u1209600.0.6.3.user.out 2> q5a/u1209600.0.6.3.user.err 

734:
	mkdir q5a/u1209600.0.6.4.user.sols;./gjsolver q5a/u1209600.0.6.4.user q5a/u1209600.0.6.4.user.sols 1> q5a/u1209600.0.6.4.user.out 2> q5a/u1209600.0.6.4.user.err 

735:
	mkdir q5a/u1209600.0.6.5.user.sols;./gjsolver q5a/u1209600.0.6.5.user q5a/u1209600.0.6.5.user.sols 1> q5a/u1209600.0.6.5.user.out 2> q5a/u1209600.0.6.5.user.err 

736:
	mkdir q5a/u1209600.0.8.1.user.sols;./gjsolver q5a/u1209600.0.8.1.user q5a/u1209600.0.8.1.user.sols 1> q5a/u1209600.0.8.1.user.out 2> q5a/u1209600.0.8.1.user.err 

737:
	mkdir q5a/u1209600.0.8.2.user.sols;./gjsolver q5a/u1209600.0.8.2.user q5a/u1209600.0.8.2.user.sols 1> q5a/u1209600.0.8.2.user.out 2> q5a/u1209600.0.8.2.user.err 

738:
	mkdir q5a/u1209600.0.8.3.user.sols;./gjsolver q5a/u1209600.0.8.3.user q5a/u1209600.0.8.3.user.sols 1> q5a/u1209600.0.8.3.user.out 2> q5a/u1209600.0.8.3.user.err 

739:
	mkdir q5a/u1209600.0.8.4.user.sols;./gjsolver q5a/u1209600.0.8.4.user q5a/u1209600.0.8.4.user.sols 1> q5a/u1209600.0.8.4.user.out 2> q5a/u1209600.0.8.4.user.err 

740:
	mkdir q5a/u1209600.0.8.5.user.sols;./gjsolver q5a/u1209600.0.8.5.user q5a/u1209600.0.8.5.user.sols 1> q5a/u1209600.0.8.5.user.out 2> q5a/u1209600.0.8.5.user.err 

741:
	mkdir q5a/u1814400.0.2.1.user.sols;./gjsolver q5a/u1814400.0.2.1.user q5a/u1814400.0.2.1.user.sols 1> q5a/u1814400.0.2.1.user.out 2> q5a/u1814400.0.2.1.user.err 

742:
	mkdir q5a/u1814400.0.2.2.user.sols;./gjsolver q5a/u1814400.0.2.2.user q5a/u1814400.0.2.2.user.sols 1> q5a/u1814400.0.2.2.user.out 2> q5a/u1814400.0.2.2.user.err 

743:
	mkdir q5a/u1814400.0.2.3.user.sols;./gjsolver q5a/u1814400.0.2.3.user q5a/u1814400.0.2.3.user.sols 1> q5a/u1814400.0.2.3.user.out 2> q5a/u1814400.0.2.3.user.err 

744:
	mkdir q5a/u1814400.0.2.4.user.sols;./gjsolver q5a/u1814400.0.2.4.user q5a/u1814400.0.2.4.user.sols 1> q5a/u1814400.0.2.4.user.out 2> q5a/u1814400.0.2.4.user.err 

745:
	mkdir q5a/u1814400.0.2.5.user.sols;./gjsolver q5a/u1814400.0.2.5.user q5a/u1814400.0.2.5.user.sols 1> q5a/u1814400.0.2.5.user.out 2> q5a/u1814400.0.2.5.user.err 

746:
	mkdir q5a/u1814400.0.4.1.user.sols;./gjsolver q5a/u1814400.0.4.1.user q5a/u1814400.0.4.1.user.sols 1> q5a/u1814400.0.4.1.user.out 2> q5a/u1814400.0.4.1.user.err 

747:
	mkdir q5a/u1814400.0.4.2.user.sols;./gjsolver q5a/u1814400.0.4.2.user q5a/u1814400.0.4.2.user.sols 1> q5a/u1814400.0.4.2.user.out 2> q5a/u1814400.0.4.2.user.err 

748:
	mkdir q5a/u1814400.0.4.3.user.sols;./gjsolver q5a/u1814400.0.4.3.user q5a/u1814400.0.4.3.user.sols 1> q5a/u1814400.0.4.3.user.out 2> q5a/u1814400.0.4.3.user.err 

749:
	mkdir q5a/u1814400.0.4.4.user.sols;./gjsolver q5a/u1814400.0.4.4.user q5a/u1814400.0.4.4.user.sols 1> q5a/u1814400.0.4.4.user.out 2> q5a/u1814400.0.4.4.user.err 

750:
	mkdir q5a/u1814400.0.4.5.user.sols;./gjsolver q5a/u1814400.0.4.5.user q5a/u1814400.0.4.5.user.sols 1> q5a/u1814400.0.4.5.user.out 2> q5a/u1814400.0.4.5.user.err 

751:
	mkdir q5a/u1814400.0.6.1.user.sols;./gjsolver q5a/u1814400.0.6.1.user q5a/u1814400.0.6.1.user.sols 1> q5a/u1814400.0.6.1.user.out 2> q5a/u1814400.0.6.1.user.err 

752:
	mkdir q5a/u1814400.0.6.2.user.sols;./gjsolver q5a/u1814400.0.6.2.user q5a/u1814400.0.6.2.user.sols 1> q5a/u1814400.0.6.2.user.out 2> q5a/u1814400.0.6.2.user.err 

753:
	mkdir q5a/u1814400.0.6.3.user.sols;./gjsolver q5a/u1814400.0.6.3.user q5a/u1814400.0.6.3.user.sols 1> q5a/u1814400.0.6.3.user.out 2> q5a/u1814400.0.6.3.user.err 

754:
	mkdir q5a/u1814400.0.6.4.user.sols;./gjsolver q5a/u1814400.0.6.4.user q5a/u1814400.0.6.4.user.sols 1> q5a/u1814400.0.6.4.user.out 2> q5a/u1814400.0.6.4.user.err 

755:
	mkdir q5a/u1814400.0.6.5.user.sols;./gjsolver q5a/u1814400.0.6.5.user q5a/u1814400.0.6.5.user.sols 1> q5a/u1814400.0.6.5.user.out 2> q5a/u1814400.0.6.5.user.err 

756:
	mkdir q5a/u1814400.0.8.1.user.sols;./gjsolver q5a/u1814400.0.8.1.user q5a/u1814400.0.8.1.user.sols 1> q5a/u1814400.0.8.1.user.out 2> q5a/u1814400.0.8.1.user.err 

757:
	mkdir q5a/u1814400.0.8.2.user.sols;./gjsolver q5a/u1814400.0.8.2.user q5a/u1814400.0.8.2.user.sols 1> q5a/u1814400.0.8.2.user.out 2> q5a/u1814400.0.8.2.user.err 

758:
	mkdir q5a/u1814400.0.8.3.user.sols;./gjsolver q5a/u1814400.0.8.3.user q5a/u1814400.0.8.3.user.sols 1> q5a/u1814400.0.8.3.user.out 2> q5a/u1814400.0.8.3.user.err 

759:
	mkdir q5a/u1814400.0.8.4.user.sols;./gjsolver q5a/u1814400.0.8.4.user q5a/u1814400.0.8.4.user.sols 1> q5a/u1814400.0.8.4.user.out 2> q5a/u1814400.0.8.4.user.err 

760:
	mkdir q5a/u1814400.0.8.5.user.sols;./gjsolver q5a/u1814400.0.8.5.user q5a/u1814400.0.8.5.user.sols 1> q5a/u1814400.0.8.5.user.out 2> q5a/u1814400.0.8.5.user.err 

761:
	mkdir q5a/u2419200.0.2.1.user.sols;./gjsolver q5a/u2419200.0.2.1.user q5a/u2419200.0.2.1.user.sols 1> q5a/u2419200.0.2.1.user.out 2> q5a/u2419200.0.2.1.user.err 

762:
	mkdir q5a/u2419200.0.2.2.user.sols;./gjsolver q5a/u2419200.0.2.2.user q5a/u2419200.0.2.2.user.sols 1> q5a/u2419200.0.2.2.user.out 2> q5a/u2419200.0.2.2.user.err 

763:
	mkdir q5a/u2419200.0.2.3.user.sols;./gjsolver q5a/u2419200.0.2.3.user q5a/u2419200.0.2.3.user.sols 1> q5a/u2419200.0.2.3.user.out 2> q5a/u2419200.0.2.3.user.err 

764:
	mkdir q5a/u2419200.0.2.4.user.sols;./gjsolver q5a/u2419200.0.2.4.user q5a/u2419200.0.2.4.user.sols 1> q5a/u2419200.0.2.4.user.out 2> q5a/u2419200.0.2.4.user.err 

765:
	mkdir q5a/u2419200.0.2.5.user.sols;./gjsolver q5a/u2419200.0.2.5.user q5a/u2419200.0.2.5.user.sols 1> q5a/u2419200.0.2.5.user.out 2> q5a/u2419200.0.2.5.user.err 

766:
	mkdir q5a/u2419200.0.4.1.user.sols;./gjsolver q5a/u2419200.0.4.1.user q5a/u2419200.0.4.1.user.sols 1> q5a/u2419200.0.4.1.user.out 2> q5a/u2419200.0.4.1.user.err 

767:
	mkdir q5a/u2419200.0.4.2.user.sols;./gjsolver q5a/u2419200.0.4.2.user q5a/u2419200.0.4.2.user.sols 1> q5a/u2419200.0.4.2.user.out 2> q5a/u2419200.0.4.2.user.err 

768:
	mkdir q5a/u2419200.0.4.3.user.sols;./gjsolver q5a/u2419200.0.4.3.user q5a/u2419200.0.4.3.user.sols 1> q5a/u2419200.0.4.3.user.out 2> q5a/u2419200.0.4.3.user.err 

769:
	mkdir q5a/u2419200.0.4.4.user.sols;./gjsolver q5a/u2419200.0.4.4.user q5a/u2419200.0.4.4.user.sols 1> q5a/u2419200.0.4.4.user.out 2> q5a/u2419200.0.4.4.user.err 

770:
	mkdir q5a/u2419200.0.4.5.user.sols;./gjsolver q5a/u2419200.0.4.5.user q5a/u2419200.0.4.5.user.sols 1> q5a/u2419200.0.4.5.user.out 2> q5a/u2419200.0.4.5.user.err 

771:
	mkdir q5a/u2419200.0.6.1.user.sols;./gjsolver q5a/u2419200.0.6.1.user q5a/u2419200.0.6.1.user.sols 1> q5a/u2419200.0.6.1.user.out 2> q5a/u2419200.0.6.1.user.err 

772:
	mkdir q5a/u2419200.0.6.2.user.sols;./gjsolver q5a/u2419200.0.6.2.user q5a/u2419200.0.6.2.user.sols 1> q5a/u2419200.0.6.2.user.out 2> q5a/u2419200.0.6.2.user.err 

773:
	mkdir q5a/u2419200.0.6.3.user.sols;./gjsolver q5a/u2419200.0.6.3.user q5a/u2419200.0.6.3.user.sols 1> q5a/u2419200.0.6.3.user.out 2> q5a/u2419200.0.6.3.user.err 

774:
	mkdir q5a/u2419200.0.6.4.user.sols;./gjsolver q5a/u2419200.0.6.4.user q5a/u2419200.0.6.4.user.sols 1> q5a/u2419200.0.6.4.user.out 2> q5a/u2419200.0.6.4.user.err 

775:
	mkdir q5a/u2419200.0.6.5.user.sols;./gjsolver q5a/u2419200.0.6.5.user q5a/u2419200.0.6.5.user.sols 1> q5a/u2419200.0.6.5.user.out 2> q5a/u2419200.0.6.5.user.err 

776:
	mkdir q5a/u2419200.0.8.1.user.sols;./gjsolver q5a/u2419200.0.8.1.user q5a/u2419200.0.8.1.user.sols 1> q5a/u2419200.0.8.1.user.out 2> q5a/u2419200.0.8.1.user.err 

777:
	mkdir q5a/u2419200.0.8.2.user.sols;./gjsolver q5a/u2419200.0.8.2.user q5a/u2419200.0.8.2.user.sols 1> q5a/u2419200.0.8.2.user.out 2> q5a/u2419200.0.8.2.user.err 

778:
	mkdir q5a/u2419200.0.8.3.user.sols;./gjsolver q5a/u2419200.0.8.3.user q5a/u2419200.0.8.3.user.sols 1> q5a/u2419200.0.8.3.user.out 2> q5a/u2419200.0.8.3.user.err 

779:
	mkdir q5a/u2419200.0.8.4.user.sols;./gjsolver q5a/u2419200.0.8.4.user q5a/u2419200.0.8.4.user.sols 1> q5a/u2419200.0.8.4.user.out 2> q5a/u2419200.0.8.4.user.err 

780:
	mkdir q5a/u2419200.0.8.5.user.sols;./gjsolver q5a/u2419200.0.8.5.user q5a/u2419200.0.8.5.user.sols 1> q5a/u2419200.0.8.5.user.out 2> q5a/u2419200.0.8.5.user.err 

781:
	mkdir q5a/u604800.0.2.1.user.sols;./gjsolver q5a/u604800.0.2.1.user q5a/u604800.0.2.1.user.sols 1> q5a/u604800.0.2.1.user.out 2> q5a/u604800.0.2.1.user.err 

782:
	mkdir q5a/u604800.0.2.2.user.sols;./gjsolver q5a/u604800.0.2.2.user q5a/u604800.0.2.2.user.sols 1> q5a/u604800.0.2.2.user.out 2> q5a/u604800.0.2.2.user.err 

783:
	mkdir q5a/u604800.0.2.3.user.sols;./gjsolver q5a/u604800.0.2.3.user q5a/u604800.0.2.3.user.sols 1> q5a/u604800.0.2.3.user.out 2> q5a/u604800.0.2.3.user.err 

784:
	mkdir q5a/u604800.0.2.4.user.sols;./gjsolver q5a/u604800.0.2.4.user q5a/u604800.0.2.4.user.sols 1> q5a/u604800.0.2.4.user.out 2> q5a/u604800.0.2.4.user.err 

785:
	mkdir q5a/u604800.0.2.5.user.sols;./gjsolver q5a/u604800.0.2.5.user q5a/u604800.0.2.5.user.sols 1> q5a/u604800.0.2.5.user.out 2> q5a/u604800.0.2.5.user.err 

786:
	mkdir q5a/u604800.0.4.1.user.sols;./gjsolver q5a/u604800.0.4.1.user q5a/u604800.0.4.1.user.sols 1> q5a/u604800.0.4.1.user.out 2> q5a/u604800.0.4.1.user.err 

787:
	mkdir q5a/u604800.0.4.2.user.sols;./gjsolver q5a/u604800.0.4.2.user q5a/u604800.0.4.2.user.sols 1> q5a/u604800.0.4.2.user.out 2> q5a/u604800.0.4.2.user.err 

788:
	mkdir q5a/u604800.0.4.3.user.sols;./gjsolver q5a/u604800.0.4.3.user q5a/u604800.0.4.3.user.sols 1> q5a/u604800.0.4.3.user.out 2> q5a/u604800.0.4.3.user.err 

789:
	mkdir q5a/u604800.0.4.4.user.sols;./gjsolver q5a/u604800.0.4.4.user q5a/u604800.0.4.4.user.sols 1> q5a/u604800.0.4.4.user.out 2> q5a/u604800.0.4.4.user.err 

790:
	mkdir q5a/u604800.0.4.5.user.sols;./gjsolver q5a/u604800.0.4.5.user q5a/u604800.0.4.5.user.sols 1> q5a/u604800.0.4.5.user.out 2> q5a/u604800.0.4.5.user.err 

791:
	mkdir q5a/u604800.0.6.1.user.sols;./gjsolver q5a/u604800.0.6.1.user q5a/u604800.0.6.1.user.sols 1> q5a/u604800.0.6.1.user.out 2> q5a/u604800.0.6.1.user.err 

792:
	mkdir q5a/u604800.0.6.2.user.sols;./gjsolver q5a/u604800.0.6.2.user q5a/u604800.0.6.2.user.sols 1> q5a/u604800.0.6.2.user.out 2> q5a/u604800.0.6.2.user.err 

793:
	mkdir q5a/u604800.0.6.3.user.sols;./gjsolver q5a/u604800.0.6.3.user q5a/u604800.0.6.3.user.sols 1> q5a/u604800.0.6.3.user.out 2> q5a/u604800.0.6.3.user.err 

794:
	mkdir q5a/u604800.0.6.4.user.sols;./gjsolver q5a/u604800.0.6.4.user q5a/u604800.0.6.4.user.sols 1> q5a/u604800.0.6.4.user.out 2> q5a/u604800.0.6.4.user.err 

795:
	mkdir q5a/u604800.0.6.5.user.sols;./gjsolver q5a/u604800.0.6.5.user q5a/u604800.0.6.5.user.sols 1> q5a/u604800.0.6.5.user.out 2> q5a/u604800.0.6.5.user.err 

796:
	mkdir q5a/u604800.0.8.1.user.sols;./gjsolver q5a/u604800.0.8.1.user q5a/u604800.0.8.1.user.sols 1> q5a/u604800.0.8.1.user.out 2> q5a/u604800.0.8.1.user.err 

797:
	mkdir q5a/u604800.0.8.2.user.sols;./gjsolver q5a/u604800.0.8.2.user q5a/u604800.0.8.2.user.sols 1> q5a/u604800.0.8.2.user.out 2> q5a/u604800.0.8.2.user.err 

798:
	mkdir q5a/u604800.0.8.3.user.sols;./gjsolver q5a/u604800.0.8.3.user q5a/u604800.0.8.3.user.sols 1> q5a/u604800.0.8.3.user.out 2> q5a/u604800.0.8.3.user.err 

799:
	mkdir q5a/u604800.0.8.4.user.sols;./gjsolver q5a/u604800.0.8.4.user q5a/u604800.0.8.4.user.sols 1> q5a/u604800.0.8.4.user.out 2> q5a/u604800.0.8.4.user.err 

800:
	mkdir q5a/u604800.0.8.5.user.sols;./gjsolver q5a/u604800.0.8.5.user q5a/u604800.0.8.5.user.sols 1> q5a/u604800.0.8.5.user.out 2> q5a/u604800.0.8.5.user.err 

801:
	mkdir q5b/u1-1209600-1.user.sols;./gjsolver q5b/u1-1209600-1.user q5b/u1-1209600-1.user.sols 1> q5b/u1-1209600-1.user.out 2> q5b/u1-1209600-1.user.err 

802:
	mkdir q5b/u1-1209600-10.user.sols;./gjsolver q5b/u1-1209600-10.user q5b/u1-1209600-10.user.sols 1> q5b/u1-1209600-10.user.out 2> q5b/u1-1209600-10.user.err 

803:
	mkdir q5b/u1-1209600-11.user.sols;./gjsolver q5b/u1-1209600-11.user q5b/u1-1209600-11.user.sols 1> q5b/u1-1209600-11.user.out 2> q5b/u1-1209600-11.user.err 

804:
	mkdir q5b/u1-1209600-12.user.sols;./gjsolver q5b/u1-1209600-12.user q5b/u1-1209600-12.user.sols 1> q5b/u1-1209600-12.user.out 2> q5b/u1-1209600-12.user.err 

805:
	mkdir q5b/u1-1209600-13.user.sols;./gjsolver q5b/u1-1209600-13.user q5b/u1-1209600-13.user.sols 1> q5b/u1-1209600-13.user.out 2> q5b/u1-1209600-13.user.err 

806:
	mkdir q5b/u1-1209600-14.user.sols;./gjsolver q5b/u1-1209600-14.user q5b/u1-1209600-14.user.sols 1> q5b/u1-1209600-14.user.out 2> q5b/u1-1209600-14.user.err 

807:
	mkdir q5b/u1-1209600-15.user.sols;./gjsolver q5b/u1-1209600-15.user q5b/u1-1209600-15.user.sols 1> q5b/u1-1209600-15.user.out 2> q5b/u1-1209600-15.user.err 

808:
	mkdir q5b/u1-1209600-16.user.sols;./gjsolver q5b/u1-1209600-16.user q5b/u1-1209600-16.user.sols 1> q5b/u1-1209600-16.user.out 2> q5b/u1-1209600-16.user.err 

809:
	mkdir q5b/u1-1209600-17.user.sols;./gjsolver q5b/u1-1209600-17.user q5b/u1-1209600-17.user.sols 1> q5b/u1-1209600-17.user.out 2> q5b/u1-1209600-17.user.err 

810:
	mkdir q5b/u1-1209600-18.user.sols;./gjsolver q5b/u1-1209600-18.user q5b/u1-1209600-18.user.sols 1> q5b/u1-1209600-18.user.out 2> q5b/u1-1209600-18.user.err 

811:
	mkdir q5b/u1-1209600-19.user.sols;./gjsolver q5b/u1-1209600-19.user q5b/u1-1209600-19.user.sols 1> q5b/u1-1209600-19.user.out 2> q5b/u1-1209600-19.user.err 

812:
	mkdir q5b/u1-1209600-2.user.sols;./gjsolver q5b/u1-1209600-2.user q5b/u1-1209600-2.user.sols 1> q5b/u1-1209600-2.user.out 2> q5b/u1-1209600-2.user.err 

813:
	mkdir q5b/u1-1209600-20.user.sols;./gjsolver q5b/u1-1209600-20.user q5b/u1-1209600-20.user.sols 1> q5b/u1-1209600-20.user.out 2> q5b/u1-1209600-20.user.err 

814:
	mkdir q5b/u1-1209600-21.user.sols;./gjsolver q5b/u1-1209600-21.user q5b/u1-1209600-21.user.sols 1> q5b/u1-1209600-21.user.out 2> q5b/u1-1209600-21.user.err 

815:
	mkdir q5b/u1-1209600-22.user.sols;./gjsolver q5b/u1-1209600-22.user q5b/u1-1209600-22.user.sols 1> q5b/u1-1209600-22.user.out 2> q5b/u1-1209600-22.user.err 

816:
	mkdir q5b/u1-1209600-23.user.sols;./gjsolver q5b/u1-1209600-23.user q5b/u1-1209600-23.user.sols 1> q5b/u1-1209600-23.user.out 2> q5b/u1-1209600-23.user.err 

817:
	mkdir q5b/u1-1209600-24.user.sols;./gjsolver q5b/u1-1209600-24.user q5b/u1-1209600-24.user.sols 1> q5b/u1-1209600-24.user.out 2> q5b/u1-1209600-24.user.err 

818:
	mkdir q5b/u1-1209600-25.user.sols;./gjsolver q5b/u1-1209600-25.user q5b/u1-1209600-25.user.sols 1> q5b/u1-1209600-25.user.out 2> q5b/u1-1209600-25.user.err 

819:
	mkdir q5b/u1-1209600-26.user.sols;./gjsolver q5b/u1-1209600-26.user q5b/u1-1209600-26.user.sols 1> q5b/u1-1209600-26.user.out 2> q5b/u1-1209600-26.user.err 

820:
	mkdir q5b/u1-1209600-27.user.sols;./gjsolver q5b/u1-1209600-27.user q5b/u1-1209600-27.user.sols 1> q5b/u1-1209600-27.user.out 2> q5b/u1-1209600-27.user.err 

821:
	mkdir q5b/u1-1209600-28.user.sols;./gjsolver q5b/u1-1209600-28.user q5b/u1-1209600-28.user.sols 1> q5b/u1-1209600-28.user.out 2> q5b/u1-1209600-28.user.err 

822:
	mkdir q5b/u1-1209600-29.user.sols;./gjsolver q5b/u1-1209600-29.user q5b/u1-1209600-29.user.sols 1> q5b/u1-1209600-29.user.out 2> q5b/u1-1209600-29.user.err 

823:
	mkdir q5b/u1-1209600-3.user.sols;./gjsolver q5b/u1-1209600-3.user q5b/u1-1209600-3.user.sols 1> q5b/u1-1209600-3.user.out 2> q5b/u1-1209600-3.user.err 

824:
	mkdir q5b/u1-1209600-30.user.sols;./gjsolver q5b/u1-1209600-30.user q5b/u1-1209600-30.user.sols 1> q5b/u1-1209600-30.user.out 2> q5b/u1-1209600-30.user.err 

825:
	mkdir q5b/u1-1209600-4.user.sols;./gjsolver q5b/u1-1209600-4.user q5b/u1-1209600-4.user.sols 1> q5b/u1-1209600-4.user.out 2> q5b/u1-1209600-4.user.err 

826:
	mkdir q5b/u1-1209600-5.user.sols;./gjsolver q5b/u1-1209600-5.user q5b/u1-1209600-5.user.sols 1> q5b/u1-1209600-5.user.out 2> q5b/u1-1209600-5.user.err 

827:
	mkdir q5b/u1-1209600-6.user.sols;./gjsolver q5b/u1-1209600-6.user q5b/u1-1209600-6.user.sols 1> q5b/u1-1209600-6.user.out 2> q5b/u1-1209600-6.user.err 

828:
	mkdir q5b/u1-1209600-7.user.sols;./gjsolver q5b/u1-1209600-7.user q5b/u1-1209600-7.user.sols 1> q5b/u1-1209600-7.user.out 2> q5b/u1-1209600-7.user.err 

829:
	mkdir q5b/u1-1209600-8.user.sols;./gjsolver q5b/u1-1209600-8.user q5b/u1-1209600-8.user.sols 1> q5b/u1-1209600-8.user.out 2> q5b/u1-1209600-8.user.err 

830:
	mkdir q5b/u1-1209600-9.user.sols;./gjsolver q5b/u1-1209600-9.user q5b/u1-1209600-9.user.sols 1> q5b/u1-1209600-9.user.out 2> q5b/u1-1209600-9.user.err 

831:
	mkdir q5b/u1-1814400-1.user.sols;./gjsolver q5b/u1-1814400-1.user q5b/u1-1814400-1.user.sols 1> q5b/u1-1814400-1.user.out 2> q5b/u1-1814400-1.user.err 

832:
	mkdir q5b/u1-1814400-10.user.sols;./gjsolver q5b/u1-1814400-10.user q5b/u1-1814400-10.user.sols 1> q5b/u1-1814400-10.user.out 2> q5b/u1-1814400-10.user.err 

833:
	mkdir q5b/u1-1814400-11.user.sols;./gjsolver q5b/u1-1814400-11.user q5b/u1-1814400-11.user.sols 1> q5b/u1-1814400-11.user.out 2> q5b/u1-1814400-11.user.err 

834:
	mkdir q5b/u1-1814400-12.user.sols;./gjsolver q5b/u1-1814400-12.user q5b/u1-1814400-12.user.sols 1> q5b/u1-1814400-12.user.out 2> q5b/u1-1814400-12.user.err 

835:
	mkdir q5b/u1-1814400-13.user.sols;./gjsolver q5b/u1-1814400-13.user q5b/u1-1814400-13.user.sols 1> q5b/u1-1814400-13.user.out 2> q5b/u1-1814400-13.user.err 

836:
	mkdir q5b/u1-1814400-14.user.sols;./gjsolver q5b/u1-1814400-14.user q5b/u1-1814400-14.user.sols 1> q5b/u1-1814400-14.user.out 2> q5b/u1-1814400-14.user.err 

837:
	mkdir q5b/u1-1814400-15.user.sols;./gjsolver q5b/u1-1814400-15.user q5b/u1-1814400-15.user.sols 1> q5b/u1-1814400-15.user.out 2> q5b/u1-1814400-15.user.err 

838:
	mkdir q5b/u1-1814400-16.user.sols;./gjsolver q5b/u1-1814400-16.user q5b/u1-1814400-16.user.sols 1> q5b/u1-1814400-16.user.out 2> q5b/u1-1814400-16.user.err 

839:
	mkdir q5b/u1-1814400-17.user.sols;./gjsolver q5b/u1-1814400-17.user q5b/u1-1814400-17.user.sols 1> q5b/u1-1814400-17.user.out 2> q5b/u1-1814400-17.user.err 

840:
	mkdir q5b/u1-1814400-18.user.sols;./gjsolver q5b/u1-1814400-18.user q5b/u1-1814400-18.user.sols 1> q5b/u1-1814400-18.user.out 2> q5b/u1-1814400-18.user.err 

841:
	mkdir q5b/u1-1814400-19.user.sols;./gjsolver q5b/u1-1814400-19.user q5b/u1-1814400-19.user.sols 1> q5b/u1-1814400-19.user.out 2> q5b/u1-1814400-19.user.err 

842:
	mkdir q5b/u1-1814400-2.user.sols;./gjsolver q5b/u1-1814400-2.user q5b/u1-1814400-2.user.sols 1> q5b/u1-1814400-2.user.out 2> q5b/u1-1814400-2.user.err 

843:
	mkdir q5b/u1-1814400-20.user.sols;./gjsolver q5b/u1-1814400-20.user q5b/u1-1814400-20.user.sols 1> q5b/u1-1814400-20.user.out 2> q5b/u1-1814400-20.user.err 

844:
	mkdir q5b/u1-1814400-21.user.sols;./gjsolver q5b/u1-1814400-21.user q5b/u1-1814400-21.user.sols 1> q5b/u1-1814400-21.user.out 2> q5b/u1-1814400-21.user.err 

845:
	mkdir q5b/u1-1814400-22.user.sols;./gjsolver q5b/u1-1814400-22.user q5b/u1-1814400-22.user.sols 1> q5b/u1-1814400-22.user.out 2> q5b/u1-1814400-22.user.err 

846:
	mkdir q5b/u1-1814400-23.user.sols;./gjsolver q5b/u1-1814400-23.user q5b/u1-1814400-23.user.sols 1> q5b/u1-1814400-23.user.out 2> q5b/u1-1814400-23.user.err 

847:
	mkdir q5b/u1-1814400-24.user.sols;./gjsolver q5b/u1-1814400-24.user q5b/u1-1814400-24.user.sols 1> q5b/u1-1814400-24.user.out 2> q5b/u1-1814400-24.user.err 

848:
	mkdir q5b/u1-1814400-25.user.sols;./gjsolver q5b/u1-1814400-25.user q5b/u1-1814400-25.user.sols 1> q5b/u1-1814400-25.user.out 2> q5b/u1-1814400-25.user.err 

849:
	mkdir q5b/u1-1814400-26.user.sols;./gjsolver q5b/u1-1814400-26.user q5b/u1-1814400-26.user.sols 1> q5b/u1-1814400-26.user.out 2> q5b/u1-1814400-26.user.err 

850:
	mkdir q5b/u1-1814400-27.user.sols;./gjsolver q5b/u1-1814400-27.user q5b/u1-1814400-27.user.sols 1> q5b/u1-1814400-27.user.out 2> q5b/u1-1814400-27.user.err 

851:
	mkdir q5b/u1-1814400-28.user.sols;./gjsolver q5b/u1-1814400-28.user q5b/u1-1814400-28.user.sols 1> q5b/u1-1814400-28.user.out 2> q5b/u1-1814400-28.user.err 

852:
	mkdir q5b/u1-1814400-29.user.sols;./gjsolver q5b/u1-1814400-29.user q5b/u1-1814400-29.user.sols 1> q5b/u1-1814400-29.user.out 2> q5b/u1-1814400-29.user.err 

853:
	mkdir q5b/u1-1814400-3.user.sols;./gjsolver q5b/u1-1814400-3.user q5b/u1-1814400-3.user.sols 1> q5b/u1-1814400-3.user.out 2> q5b/u1-1814400-3.user.err 

854:
	mkdir q5b/u1-1814400-30.user.sols;./gjsolver q5b/u1-1814400-30.user q5b/u1-1814400-30.user.sols 1> q5b/u1-1814400-30.user.out 2> q5b/u1-1814400-30.user.err 

855:
	mkdir q5b/u1-1814400-4.user.sols;./gjsolver q5b/u1-1814400-4.user q5b/u1-1814400-4.user.sols 1> q5b/u1-1814400-4.user.out 2> q5b/u1-1814400-4.user.err 

856:
	mkdir q5b/u1-1814400-5.user.sols;./gjsolver q5b/u1-1814400-5.user q5b/u1-1814400-5.user.sols 1> q5b/u1-1814400-5.user.out 2> q5b/u1-1814400-5.user.err 

857:
	mkdir q5b/u1-1814400-6.user.sols;./gjsolver q5b/u1-1814400-6.user q5b/u1-1814400-6.user.sols 1> q5b/u1-1814400-6.user.out 2> q5b/u1-1814400-6.user.err 

858:
	mkdir q5b/u1-1814400-7.user.sols;./gjsolver q5b/u1-1814400-7.user q5b/u1-1814400-7.user.sols 1> q5b/u1-1814400-7.user.out 2> q5b/u1-1814400-7.user.err 

859:
	mkdir q5b/u1-1814400-8.user.sols;./gjsolver q5b/u1-1814400-8.user q5b/u1-1814400-8.user.sols 1> q5b/u1-1814400-8.user.out 2> q5b/u1-1814400-8.user.err 

860:
	mkdir q5b/u1-1814400-9.user.sols;./gjsolver q5b/u1-1814400-9.user q5b/u1-1814400-9.user.sols 1> q5b/u1-1814400-9.user.out 2> q5b/u1-1814400-9.user.err 

861:
	mkdir q5b/u1-2419200-1.user.sols;./gjsolver q5b/u1-2419200-1.user q5b/u1-2419200-1.user.sols 1> q5b/u1-2419200-1.user.out 2> q5b/u1-2419200-1.user.err 

862:
	mkdir q5b/u1-2419200-10.user.sols;./gjsolver q5b/u1-2419200-10.user q5b/u1-2419200-10.user.sols 1> q5b/u1-2419200-10.user.out 2> q5b/u1-2419200-10.user.err 

863:
	mkdir q5b/u1-2419200-11.user.sols;./gjsolver q5b/u1-2419200-11.user q5b/u1-2419200-11.user.sols 1> q5b/u1-2419200-11.user.out 2> q5b/u1-2419200-11.user.err 

864:
	mkdir q5b/u1-2419200-12.user.sols;./gjsolver q5b/u1-2419200-12.user q5b/u1-2419200-12.user.sols 1> q5b/u1-2419200-12.user.out 2> q5b/u1-2419200-12.user.err 

865:
	mkdir q5b/u1-2419200-13.user.sols;./gjsolver q5b/u1-2419200-13.user q5b/u1-2419200-13.user.sols 1> q5b/u1-2419200-13.user.out 2> q5b/u1-2419200-13.user.err 

866:
	mkdir q5b/u1-2419200-14.user.sols;./gjsolver q5b/u1-2419200-14.user q5b/u1-2419200-14.user.sols 1> q5b/u1-2419200-14.user.out 2> q5b/u1-2419200-14.user.err 

867:
	mkdir q5b/u1-2419200-15.user.sols;./gjsolver q5b/u1-2419200-15.user q5b/u1-2419200-15.user.sols 1> q5b/u1-2419200-15.user.out 2> q5b/u1-2419200-15.user.err 

868:
	mkdir q5b/u1-2419200-16.user.sols;./gjsolver q5b/u1-2419200-16.user q5b/u1-2419200-16.user.sols 1> q5b/u1-2419200-16.user.out 2> q5b/u1-2419200-16.user.err 

869:
	mkdir q5b/u1-2419200-17.user.sols;./gjsolver q5b/u1-2419200-17.user q5b/u1-2419200-17.user.sols 1> q5b/u1-2419200-17.user.out 2> q5b/u1-2419200-17.user.err 

870:
	mkdir q5b/u1-2419200-18.user.sols;./gjsolver q5b/u1-2419200-18.user q5b/u1-2419200-18.user.sols 1> q5b/u1-2419200-18.user.out 2> q5b/u1-2419200-18.user.err 

871:
	mkdir q5b/u1-2419200-19.user.sols;./gjsolver q5b/u1-2419200-19.user q5b/u1-2419200-19.user.sols 1> q5b/u1-2419200-19.user.out 2> q5b/u1-2419200-19.user.err 

872:
	mkdir q5b/u1-2419200-2.user.sols;./gjsolver q5b/u1-2419200-2.user q5b/u1-2419200-2.user.sols 1> q5b/u1-2419200-2.user.out 2> q5b/u1-2419200-2.user.err 

873:
	mkdir q5b/u1-2419200-20.user.sols;./gjsolver q5b/u1-2419200-20.user q5b/u1-2419200-20.user.sols 1> q5b/u1-2419200-20.user.out 2> q5b/u1-2419200-20.user.err 

874:
	mkdir q5b/u1-2419200-21.user.sols;./gjsolver q5b/u1-2419200-21.user q5b/u1-2419200-21.user.sols 1> q5b/u1-2419200-21.user.out 2> q5b/u1-2419200-21.user.err 

875:
	mkdir q5b/u1-2419200-22.user.sols;./gjsolver q5b/u1-2419200-22.user q5b/u1-2419200-22.user.sols 1> q5b/u1-2419200-22.user.out 2> q5b/u1-2419200-22.user.err 

876:
	mkdir q5b/u1-2419200-23.user.sols;./gjsolver q5b/u1-2419200-23.user q5b/u1-2419200-23.user.sols 1> q5b/u1-2419200-23.user.out 2> q5b/u1-2419200-23.user.err 

877:
	mkdir q5b/u1-2419200-24.user.sols;./gjsolver q5b/u1-2419200-24.user q5b/u1-2419200-24.user.sols 1> q5b/u1-2419200-24.user.out 2> q5b/u1-2419200-24.user.err 

878:
	mkdir q5b/u1-2419200-25.user.sols;./gjsolver q5b/u1-2419200-25.user q5b/u1-2419200-25.user.sols 1> q5b/u1-2419200-25.user.out 2> q5b/u1-2419200-25.user.err 

879:
	mkdir q5b/u1-2419200-26.user.sols;./gjsolver q5b/u1-2419200-26.user q5b/u1-2419200-26.user.sols 1> q5b/u1-2419200-26.user.out 2> q5b/u1-2419200-26.user.err 

880:
	mkdir q5b/u1-2419200-27.user.sols;./gjsolver q5b/u1-2419200-27.user q5b/u1-2419200-27.user.sols 1> q5b/u1-2419200-27.user.out 2> q5b/u1-2419200-27.user.err 

881:
	mkdir q5b/u1-2419200-28.user.sols;./gjsolver q5b/u1-2419200-28.user q5b/u1-2419200-28.user.sols 1> q5b/u1-2419200-28.user.out 2> q5b/u1-2419200-28.user.err 

882:
	mkdir q5b/u1-2419200-29.user.sols;./gjsolver q5b/u1-2419200-29.user q5b/u1-2419200-29.user.sols 1> q5b/u1-2419200-29.user.out 2> q5b/u1-2419200-29.user.err 

883:
	mkdir q5b/u1-2419200-3.user.sols;./gjsolver q5b/u1-2419200-3.user q5b/u1-2419200-3.user.sols 1> q5b/u1-2419200-3.user.out 2> q5b/u1-2419200-3.user.err 

884:
	mkdir q5b/u1-2419200-30.user.sols;./gjsolver q5b/u1-2419200-30.user q5b/u1-2419200-30.user.sols 1> q5b/u1-2419200-30.user.out 2> q5b/u1-2419200-30.user.err 

885:
	mkdir q5b/u1-2419200-4.user.sols;./gjsolver q5b/u1-2419200-4.user q5b/u1-2419200-4.user.sols 1> q5b/u1-2419200-4.user.out 2> q5b/u1-2419200-4.user.err 

886:
	mkdir q5b/u1-2419200-5.user.sols;./gjsolver q5b/u1-2419200-5.user q5b/u1-2419200-5.user.sols 1> q5b/u1-2419200-5.user.out 2> q5b/u1-2419200-5.user.err 

887:
	mkdir q5b/u1-2419200-6.user.sols;./gjsolver q5b/u1-2419200-6.user q5b/u1-2419200-6.user.sols 1> q5b/u1-2419200-6.user.out 2> q5b/u1-2419200-6.user.err 

888:
	mkdir q5b/u1-2419200-7.user.sols;./gjsolver q5b/u1-2419200-7.user q5b/u1-2419200-7.user.sols 1> q5b/u1-2419200-7.user.out 2> q5b/u1-2419200-7.user.err 

889:
	mkdir q5b/u1-2419200-8.user.sols;./gjsolver q5b/u1-2419200-8.user q5b/u1-2419200-8.user.sols 1> q5b/u1-2419200-8.user.out 2> q5b/u1-2419200-8.user.err 

890:
	mkdir q5b/u1-2419200-9.user.sols;./gjsolver q5b/u1-2419200-9.user q5b/u1-2419200-9.user.sols 1> q5b/u1-2419200-9.user.out 2> q5b/u1-2419200-9.user.err 

891:
	mkdir q5b/u1-604800-1.user.sols;./gjsolver q5b/u1-604800-1.user q5b/u1-604800-1.user.sols 1> q5b/u1-604800-1.user.out 2> q5b/u1-604800-1.user.err 

892:
	mkdir q5b/u1-604800-10.user.sols;./gjsolver q5b/u1-604800-10.user q5b/u1-604800-10.user.sols 1> q5b/u1-604800-10.user.out 2> q5b/u1-604800-10.user.err 

893:
	mkdir q5b/u1-604800-11.user.sols;./gjsolver q5b/u1-604800-11.user q5b/u1-604800-11.user.sols 1> q5b/u1-604800-11.user.out 2> q5b/u1-604800-11.user.err 

894:
	mkdir q5b/u1-604800-12.user.sols;./gjsolver q5b/u1-604800-12.user q5b/u1-604800-12.user.sols 1> q5b/u1-604800-12.user.out 2> q5b/u1-604800-12.user.err 

895:
	mkdir q5b/u1-604800-13.user.sols;./gjsolver q5b/u1-604800-13.user q5b/u1-604800-13.user.sols 1> q5b/u1-604800-13.user.out 2> q5b/u1-604800-13.user.err 

896:
	mkdir q5b/u1-604800-14.user.sols;./gjsolver q5b/u1-604800-14.user q5b/u1-604800-14.user.sols 1> q5b/u1-604800-14.user.out 2> q5b/u1-604800-14.user.err 

897:
	mkdir q5b/u1-604800-15.user.sols;./gjsolver q5b/u1-604800-15.user q5b/u1-604800-15.user.sols 1> q5b/u1-604800-15.user.out 2> q5b/u1-604800-15.user.err 

898:
	mkdir q5b/u1-604800-16.user.sols;./gjsolver q5b/u1-604800-16.user q5b/u1-604800-16.user.sols 1> q5b/u1-604800-16.user.out 2> q5b/u1-604800-16.user.err 

899:
	mkdir q5b/u1-604800-17.user.sols;./gjsolver q5b/u1-604800-17.user q5b/u1-604800-17.user.sols 1> q5b/u1-604800-17.user.out 2> q5b/u1-604800-17.user.err 

900:
	mkdir q5b/u1-604800-18.user.sols;./gjsolver q5b/u1-604800-18.user q5b/u1-604800-18.user.sols 1> q5b/u1-604800-18.user.out 2> q5b/u1-604800-18.user.err 

901:
	mkdir q5b/u1-604800-19.user.sols;./gjsolver q5b/u1-604800-19.user q5b/u1-604800-19.user.sols 1> q5b/u1-604800-19.user.out 2> q5b/u1-604800-19.user.err 

902:
	mkdir q5b/u1-604800-2.user.sols;./gjsolver q5b/u1-604800-2.user q5b/u1-604800-2.user.sols 1> q5b/u1-604800-2.user.out 2> q5b/u1-604800-2.user.err 

903:
	mkdir q5b/u1-604800-20.user.sols;./gjsolver q5b/u1-604800-20.user q5b/u1-604800-20.user.sols 1> q5b/u1-604800-20.user.out 2> q5b/u1-604800-20.user.err 

904:
	mkdir q5b/u1-604800-21.user.sols;./gjsolver q5b/u1-604800-21.user q5b/u1-604800-21.user.sols 1> q5b/u1-604800-21.user.out 2> q5b/u1-604800-21.user.err 

905:
	mkdir q5b/u1-604800-22.user.sols;./gjsolver q5b/u1-604800-22.user q5b/u1-604800-22.user.sols 1> q5b/u1-604800-22.user.out 2> q5b/u1-604800-22.user.err 

906:
	mkdir q5b/u1-604800-23.user.sols;./gjsolver q5b/u1-604800-23.user q5b/u1-604800-23.user.sols 1> q5b/u1-604800-23.user.out 2> q5b/u1-604800-23.user.err 

907:
	mkdir q5b/u1-604800-24.user.sols;./gjsolver q5b/u1-604800-24.user q5b/u1-604800-24.user.sols 1> q5b/u1-604800-24.user.out 2> q5b/u1-604800-24.user.err 

908:
	mkdir q5b/u1-604800-25.user.sols;./gjsolver q5b/u1-604800-25.user q5b/u1-604800-25.user.sols 1> q5b/u1-604800-25.user.out 2> q5b/u1-604800-25.user.err 

909:
	mkdir q5b/u1-604800-26.user.sols;./gjsolver q5b/u1-604800-26.user q5b/u1-604800-26.user.sols 1> q5b/u1-604800-26.user.out 2> q5b/u1-604800-26.user.err 

910:
	mkdir q5b/u1-604800-27.user.sols;./gjsolver q5b/u1-604800-27.user q5b/u1-604800-27.user.sols 1> q5b/u1-604800-27.user.out 2> q5b/u1-604800-27.user.err 

911:
	mkdir q5b/u1-604800-28.user.sols;./gjsolver q5b/u1-604800-28.user q5b/u1-604800-28.user.sols 1> q5b/u1-604800-28.user.out 2> q5b/u1-604800-28.user.err 

912:
	mkdir q5b/u1-604800-29.user.sols;./gjsolver q5b/u1-604800-29.user q5b/u1-604800-29.user.sols 1> q5b/u1-604800-29.user.out 2> q5b/u1-604800-29.user.err 

913:
	mkdir q5b/u1-604800-3.user.sols;./gjsolver q5b/u1-604800-3.user q5b/u1-604800-3.user.sols 1> q5b/u1-604800-3.user.out 2> q5b/u1-604800-3.user.err 

914:
	mkdir q5b/u1-604800-30.user.sols;./gjsolver q5b/u1-604800-30.user q5b/u1-604800-30.user.sols 1> q5b/u1-604800-30.user.out 2> q5b/u1-604800-30.user.err 

915:
	mkdir q5b/u1-604800-4.user.sols;./gjsolver q5b/u1-604800-4.user q5b/u1-604800-4.user.sols 1> q5b/u1-604800-4.user.out 2> q5b/u1-604800-4.user.err 

916:
	mkdir q5b/u1-604800-5.user.sols;./gjsolver q5b/u1-604800-5.user q5b/u1-604800-5.user.sols 1> q5b/u1-604800-5.user.out 2> q5b/u1-604800-5.user.err 

917:
	mkdir q5b/u1-604800-6.user.sols;./gjsolver q5b/u1-604800-6.user q5b/u1-604800-6.user.sols 1> q5b/u1-604800-6.user.out 2> q5b/u1-604800-6.user.err 

918:
	mkdir q5b/u1-604800-7.user.sols;./gjsolver q5b/u1-604800-7.user q5b/u1-604800-7.user.sols 1> q5b/u1-604800-7.user.out 2> q5b/u1-604800-7.user.err 

919:
	mkdir q5b/u1-604800-8.user.sols;./gjsolver q5b/u1-604800-8.user q5b/u1-604800-8.user.sols 1> q5b/u1-604800-8.user.out 2> q5b/u1-604800-8.user.err 

920:
	mkdir q5b/u1-604800-9.user.sols;./gjsolver q5b/u1-604800-9.user q5b/u1-604800-9.user.sols 1> q5b/u1-604800-9.user.out 2> q5b/u1-604800-9.user.err 

921:
	mkdir q5b/u2-1209600-1.user.sols;./gjsolver q5b/u2-1209600-1.user q5b/u2-1209600-1.user.sols 1> q5b/u2-1209600-1.user.out 2> q5b/u2-1209600-1.user.err 

922:
	mkdir q5b/u2-1209600-10.user.sols;./gjsolver q5b/u2-1209600-10.user q5b/u2-1209600-10.user.sols 1> q5b/u2-1209600-10.user.out 2> q5b/u2-1209600-10.user.err 

923:
	mkdir q5b/u2-1209600-11.user.sols;./gjsolver q5b/u2-1209600-11.user q5b/u2-1209600-11.user.sols 1> q5b/u2-1209600-11.user.out 2> q5b/u2-1209600-11.user.err 

924:
	mkdir q5b/u2-1209600-12.user.sols;./gjsolver q5b/u2-1209600-12.user q5b/u2-1209600-12.user.sols 1> q5b/u2-1209600-12.user.out 2> q5b/u2-1209600-12.user.err 

925:
	mkdir q5b/u2-1209600-13.user.sols;./gjsolver q5b/u2-1209600-13.user q5b/u2-1209600-13.user.sols 1> q5b/u2-1209600-13.user.out 2> q5b/u2-1209600-13.user.err 

926:
	mkdir q5b/u2-1209600-14.user.sols;./gjsolver q5b/u2-1209600-14.user q5b/u2-1209600-14.user.sols 1> q5b/u2-1209600-14.user.out 2> q5b/u2-1209600-14.user.err 

927:
	mkdir q5b/u2-1209600-15.user.sols;./gjsolver q5b/u2-1209600-15.user q5b/u2-1209600-15.user.sols 1> q5b/u2-1209600-15.user.out 2> q5b/u2-1209600-15.user.err 

928:
	mkdir q5b/u2-1209600-16.user.sols;./gjsolver q5b/u2-1209600-16.user q5b/u2-1209600-16.user.sols 1> q5b/u2-1209600-16.user.out 2> q5b/u2-1209600-16.user.err 

929:
	mkdir q5b/u2-1209600-17.user.sols;./gjsolver q5b/u2-1209600-17.user q5b/u2-1209600-17.user.sols 1> q5b/u2-1209600-17.user.out 2> q5b/u2-1209600-17.user.err 

930:
	mkdir q5b/u2-1209600-18.user.sols;./gjsolver q5b/u2-1209600-18.user q5b/u2-1209600-18.user.sols 1> q5b/u2-1209600-18.user.out 2> q5b/u2-1209600-18.user.err 

931:
	mkdir q5b/u2-1209600-19.user.sols;./gjsolver q5b/u2-1209600-19.user q5b/u2-1209600-19.user.sols 1> q5b/u2-1209600-19.user.out 2> q5b/u2-1209600-19.user.err 

932:
	mkdir q5b/u2-1209600-2.user.sols;./gjsolver q5b/u2-1209600-2.user q5b/u2-1209600-2.user.sols 1> q5b/u2-1209600-2.user.out 2> q5b/u2-1209600-2.user.err 

933:
	mkdir q5b/u2-1209600-20.user.sols;./gjsolver q5b/u2-1209600-20.user q5b/u2-1209600-20.user.sols 1> q5b/u2-1209600-20.user.out 2> q5b/u2-1209600-20.user.err 

934:
	mkdir q5b/u2-1209600-21.user.sols;./gjsolver q5b/u2-1209600-21.user q5b/u2-1209600-21.user.sols 1> q5b/u2-1209600-21.user.out 2> q5b/u2-1209600-21.user.err 

935:
	mkdir q5b/u2-1209600-22.user.sols;./gjsolver q5b/u2-1209600-22.user q5b/u2-1209600-22.user.sols 1> q5b/u2-1209600-22.user.out 2> q5b/u2-1209600-22.user.err 

936:
	mkdir q5b/u2-1209600-23.user.sols;./gjsolver q5b/u2-1209600-23.user q5b/u2-1209600-23.user.sols 1> q5b/u2-1209600-23.user.out 2> q5b/u2-1209600-23.user.err 

937:
	mkdir q5b/u2-1209600-24.user.sols;./gjsolver q5b/u2-1209600-24.user q5b/u2-1209600-24.user.sols 1> q5b/u2-1209600-24.user.out 2> q5b/u2-1209600-24.user.err 

938:
	mkdir q5b/u2-1209600-25.user.sols;./gjsolver q5b/u2-1209600-25.user q5b/u2-1209600-25.user.sols 1> q5b/u2-1209600-25.user.out 2> q5b/u2-1209600-25.user.err 

939:
	mkdir q5b/u2-1209600-26.user.sols;./gjsolver q5b/u2-1209600-26.user q5b/u2-1209600-26.user.sols 1> q5b/u2-1209600-26.user.out 2> q5b/u2-1209600-26.user.err 

940:
	mkdir q5b/u2-1209600-27.user.sols;./gjsolver q5b/u2-1209600-27.user q5b/u2-1209600-27.user.sols 1> q5b/u2-1209600-27.user.out 2> q5b/u2-1209600-27.user.err 

941:
	mkdir q5b/u2-1209600-28.user.sols;./gjsolver q5b/u2-1209600-28.user q5b/u2-1209600-28.user.sols 1> q5b/u2-1209600-28.user.out 2> q5b/u2-1209600-28.user.err 

942:
	mkdir q5b/u2-1209600-29.user.sols;./gjsolver q5b/u2-1209600-29.user q5b/u2-1209600-29.user.sols 1> q5b/u2-1209600-29.user.out 2> q5b/u2-1209600-29.user.err 

943:
	mkdir q5b/u2-1209600-3.user.sols;./gjsolver q5b/u2-1209600-3.user q5b/u2-1209600-3.user.sols 1> q5b/u2-1209600-3.user.out 2> q5b/u2-1209600-3.user.err 

944:
	mkdir q5b/u2-1209600-30.user.sols;./gjsolver q5b/u2-1209600-30.user q5b/u2-1209600-30.user.sols 1> q5b/u2-1209600-30.user.out 2> q5b/u2-1209600-30.user.err 

945:
	mkdir q5b/u2-1209600-4.user.sols;./gjsolver q5b/u2-1209600-4.user q5b/u2-1209600-4.user.sols 1> q5b/u2-1209600-4.user.out 2> q5b/u2-1209600-4.user.err 

946:
	mkdir q5b/u2-1209600-5.user.sols;./gjsolver q5b/u2-1209600-5.user q5b/u2-1209600-5.user.sols 1> q5b/u2-1209600-5.user.out 2> q5b/u2-1209600-5.user.err 

947:
	mkdir q5b/u2-1209600-6.user.sols;./gjsolver q5b/u2-1209600-6.user q5b/u2-1209600-6.user.sols 1> q5b/u2-1209600-6.user.out 2> q5b/u2-1209600-6.user.err 

948:
	mkdir q5b/u2-1209600-7.user.sols;./gjsolver q5b/u2-1209600-7.user q5b/u2-1209600-7.user.sols 1> q5b/u2-1209600-7.user.out 2> q5b/u2-1209600-7.user.err 

949:
	mkdir q5b/u2-1209600-8.user.sols;./gjsolver q5b/u2-1209600-8.user q5b/u2-1209600-8.user.sols 1> q5b/u2-1209600-8.user.out 2> q5b/u2-1209600-8.user.err 

950:
	mkdir q5b/u2-1209600-9.user.sols;./gjsolver q5b/u2-1209600-9.user q5b/u2-1209600-9.user.sols 1> q5b/u2-1209600-9.user.out 2> q5b/u2-1209600-9.user.err 

951:
	mkdir q5b/u2-1814400-1.user.sols;./gjsolver q5b/u2-1814400-1.user q5b/u2-1814400-1.user.sols 1> q5b/u2-1814400-1.user.out 2> q5b/u2-1814400-1.user.err 

952:
	mkdir q5b/u2-1814400-10.user.sols;./gjsolver q5b/u2-1814400-10.user q5b/u2-1814400-10.user.sols 1> q5b/u2-1814400-10.user.out 2> q5b/u2-1814400-10.user.err 

953:
	mkdir q5b/u2-1814400-11.user.sols;./gjsolver q5b/u2-1814400-11.user q5b/u2-1814400-11.user.sols 1> q5b/u2-1814400-11.user.out 2> q5b/u2-1814400-11.user.err 

954:
	mkdir q5b/u2-1814400-12.user.sols;./gjsolver q5b/u2-1814400-12.user q5b/u2-1814400-12.user.sols 1> q5b/u2-1814400-12.user.out 2> q5b/u2-1814400-12.user.err 

955:
	mkdir q5b/u2-1814400-13.user.sols;./gjsolver q5b/u2-1814400-13.user q5b/u2-1814400-13.user.sols 1> q5b/u2-1814400-13.user.out 2> q5b/u2-1814400-13.user.err 

956:
	mkdir q5b/u2-1814400-14.user.sols;./gjsolver q5b/u2-1814400-14.user q5b/u2-1814400-14.user.sols 1> q5b/u2-1814400-14.user.out 2> q5b/u2-1814400-14.user.err 

957:
	mkdir q5b/u2-1814400-15.user.sols;./gjsolver q5b/u2-1814400-15.user q5b/u2-1814400-15.user.sols 1> q5b/u2-1814400-15.user.out 2> q5b/u2-1814400-15.user.err 

958:
	mkdir q5b/u2-1814400-16.user.sols;./gjsolver q5b/u2-1814400-16.user q5b/u2-1814400-16.user.sols 1> q5b/u2-1814400-16.user.out 2> q5b/u2-1814400-16.user.err 

959:
	mkdir q5b/u2-1814400-17.user.sols;./gjsolver q5b/u2-1814400-17.user q5b/u2-1814400-17.user.sols 1> q5b/u2-1814400-17.user.out 2> q5b/u2-1814400-17.user.err 

960:
	mkdir q5b/u2-1814400-18.user.sols;./gjsolver q5b/u2-1814400-18.user q5b/u2-1814400-18.user.sols 1> q5b/u2-1814400-18.user.out 2> q5b/u2-1814400-18.user.err 

961:
	mkdir q5b/u2-1814400-19.user.sols;./gjsolver q5b/u2-1814400-19.user q5b/u2-1814400-19.user.sols 1> q5b/u2-1814400-19.user.out 2> q5b/u2-1814400-19.user.err 

962:
	mkdir q5b/u2-1814400-2.user.sols;./gjsolver q5b/u2-1814400-2.user q5b/u2-1814400-2.user.sols 1> q5b/u2-1814400-2.user.out 2> q5b/u2-1814400-2.user.err 

963:
	mkdir q5b/u2-1814400-20.user.sols;./gjsolver q5b/u2-1814400-20.user q5b/u2-1814400-20.user.sols 1> q5b/u2-1814400-20.user.out 2> q5b/u2-1814400-20.user.err 

964:
	mkdir q5b/u2-1814400-21.user.sols;./gjsolver q5b/u2-1814400-21.user q5b/u2-1814400-21.user.sols 1> q5b/u2-1814400-21.user.out 2> q5b/u2-1814400-21.user.err 

965:
	mkdir q5b/u2-1814400-22.user.sols;./gjsolver q5b/u2-1814400-22.user q5b/u2-1814400-22.user.sols 1> q5b/u2-1814400-22.user.out 2> q5b/u2-1814400-22.user.err 

966:
	mkdir q5b/u2-1814400-23.user.sols;./gjsolver q5b/u2-1814400-23.user q5b/u2-1814400-23.user.sols 1> q5b/u2-1814400-23.user.out 2> q5b/u2-1814400-23.user.err 

967:
	mkdir q5b/u2-1814400-24.user.sols;./gjsolver q5b/u2-1814400-24.user q5b/u2-1814400-24.user.sols 1> q5b/u2-1814400-24.user.out 2> q5b/u2-1814400-24.user.err 

968:
	mkdir q5b/u2-1814400-25.user.sols;./gjsolver q5b/u2-1814400-25.user q5b/u2-1814400-25.user.sols 1> q5b/u2-1814400-25.user.out 2> q5b/u2-1814400-25.user.err 

969:
	mkdir q5b/u2-1814400-26.user.sols;./gjsolver q5b/u2-1814400-26.user q5b/u2-1814400-26.user.sols 1> q5b/u2-1814400-26.user.out 2> q5b/u2-1814400-26.user.err 

970:
	mkdir q5b/u2-1814400-27.user.sols;./gjsolver q5b/u2-1814400-27.user q5b/u2-1814400-27.user.sols 1> q5b/u2-1814400-27.user.out 2> q5b/u2-1814400-27.user.err 

971:
	mkdir q5b/u2-1814400-28.user.sols;./gjsolver q5b/u2-1814400-28.user q5b/u2-1814400-28.user.sols 1> q5b/u2-1814400-28.user.out 2> q5b/u2-1814400-28.user.err 

972:
	mkdir q5b/u2-1814400-29.user.sols;./gjsolver q5b/u2-1814400-29.user q5b/u2-1814400-29.user.sols 1> q5b/u2-1814400-29.user.out 2> q5b/u2-1814400-29.user.err 

973:
	mkdir q5b/u2-1814400-3.user.sols;./gjsolver q5b/u2-1814400-3.user q5b/u2-1814400-3.user.sols 1> q5b/u2-1814400-3.user.out 2> q5b/u2-1814400-3.user.err 

974:
	mkdir q5b/u2-1814400-30.user.sols;./gjsolver q5b/u2-1814400-30.user q5b/u2-1814400-30.user.sols 1> q5b/u2-1814400-30.user.out 2> q5b/u2-1814400-30.user.err 

975:
	mkdir q5b/u2-1814400-4.user.sols;./gjsolver q5b/u2-1814400-4.user q5b/u2-1814400-4.user.sols 1> q5b/u2-1814400-4.user.out 2> q5b/u2-1814400-4.user.err 

976:
	mkdir q5b/u2-1814400-5.user.sols;./gjsolver q5b/u2-1814400-5.user q5b/u2-1814400-5.user.sols 1> q5b/u2-1814400-5.user.out 2> q5b/u2-1814400-5.user.err 

977:
	mkdir q5b/u2-1814400-6.user.sols;./gjsolver q5b/u2-1814400-6.user q5b/u2-1814400-6.user.sols 1> q5b/u2-1814400-6.user.out 2> q5b/u2-1814400-6.user.err 

978:
	mkdir q5b/u2-1814400-7.user.sols;./gjsolver q5b/u2-1814400-7.user q5b/u2-1814400-7.user.sols 1> q5b/u2-1814400-7.user.out 2> q5b/u2-1814400-7.user.err 

979:
	mkdir q5b/u2-1814400-8.user.sols;./gjsolver q5b/u2-1814400-8.user q5b/u2-1814400-8.user.sols 1> q5b/u2-1814400-8.user.out 2> q5b/u2-1814400-8.user.err 

980:
	mkdir q5b/u2-1814400-9.user.sols;./gjsolver q5b/u2-1814400-9.user q5b/u2-1814400-9.user.sols 1> q5b/u2-1814400-9.user.out 2> q5b/u2-1814400-9.user.err 

981:
	mkdir q5b/u2-2419200-1.user.sols;./gjsolver q5b/u2-2419200-1.user q5b/u2-2419200-1.user.sols 1> q5b/u2-2419200-1.user.out 2> q5b/u2-2419200-1.user.err 

982:
	mkdir q5b/u2-2419200-10.user.sols;./gjsolver q5b/u2-2419200-10.user q5b/u2-2419200-10.user.sols 1> q5b/u2-2419200-10.user.out 2> q5b/u2-2419200-10.user.err 

983:
	mkdir q5b/u2-2419200-11.user.sols;./gjsolver q5b/u2-2419200-11.user q5b/u2-2419200-11.user.sols 1> q5b/u2-2419200-11.user.out 2> q5b/u2-2419200-11.user.err 

984:
	mkdir q5b/u2-2419200-12.user.sols;./gjsolver q5b/u2-2419200-12.user q5b/u2-2419200-12.user.sols 1> q5b/u2-2419200-12.user.out 2> q5b/u2-2419200-12.user.err 

985:
	mkdir q5b/u2-2419200-13.user.sols;./gjsolver q5b/u2-2419200-13.user q5b/u2-2419200-13.user.sols 1> q5b/u2-2419200-13.user.out 2> q5b/u2-2419200-13.user.err 

986:
	mkdir q5b/u2-2419200-14.user.sols;./gjsolver q5b/u2-2419200-14.user q5b/u2-2419200-14.user.sols 1> q5b/u2-2419200-14.user.out 2> q5b/u2-2419200-14.user.err 

987:
	mkdir q5b/u2-2419200-15.user.sols;./gjsolver q5b/u2-2419200-15.user q5b/u2-2419200-15.user.sols 1> q5b/u2-2419200-15.user.out 2> q5b/u2-2419200-15.user.err 

988:
	mkdir q5b/u2-2419200-16.user.sols;./gjsolver q5b/u2-2419200-16.user q5b/u2-2419200-16.user.sols 1> q5b/u2-2419200-16.user.out 2> q5b/u2-2419200-16.user.err 

989:
	mkdir q5b/u2-2419200-17.user.sols;./gjsolver q5b/u2-2419200-17.user q5b/u2-2419200-17.user.sols 1> q5b/u2-2419200-17.user.out 2> q5b/u2-2419200-17.user.err 

990:
	mkdir q5b/u2-2419200-18.user.sols;./gjsolver q5b/u2-2419200-18.user q5b/u2-2419200-18.user.sols 1> q5b/u2-2419200-18.user.out 2> q5b/u2-2419200-18.user.err 

991:
	mkdir q5b/u2-2419200-19.user.sols;./gjsolver q5b/u2-2419200-19.user q5b/u2-2419200-19.user.sols 1> q5b/u2-2419200-19.user.out 2> q5b/u2-2419200-19.user.err 

992:
	mkdir q5b/u2-2419200-2.user.sols;./gjsolver q5b/u2-2419200-2.user q5b/u2-2419200-2.user.sols 1> q5b/u2-2419200-2.user.out 2> q5b/u2-2419200-2.user.err 

993:
	mkdir q5b/u2-2419200-20.user.sols;./gjsolver q5b/u2-2419200-20.user q5b/u2-2419200-20.user.sols 1> q5b/u2-2419200-20.user.out 2> q5b/u2-2419200-20.user.err 

994:
	mkdir q5b/u2-2419200-21.user.sols;./gjsolver q5b/u2-2419200-21.user q5b/u2-2419200-21.user.sols 1> q5b/u2-2419200-21.user.out 2> q5b/u2-2419200-21.user.err 

995:
	mkdir q5b/u2-2419200-22.user.sols;./gjsolver q5b/u2-2419200-22.user q5b/u2-2419200-22.user.sols 1> q5b/u2-2419200-22.user.out 2> q5b/u2-2419200-22.user.err 

996:
	mkdir q5b/u2-2419200-23.user.sols;./gjsolver q5b/u2-2419200-23.user q5b/u2-2419200-23.user.sols 1> q5b/u2-2419200-23.user.out 2> q5b/u2-2419200-23.user.err 

997:
	mkdir q5b/u2-2419200-24.user.sols;./gjsolver q5b/u2-2419200-24.user q5b/u2-2419200-24.user.sols 1> q5b/u2-2419200-24.user.out 2> q5b/u2-2419200-24.user.err 

998:
	mkdir q5b/u2-2419200-25.user.sols;./gjsolver q5b/u2-2419200-25.user q5b/u2-2419200-25.user.sols 1> q5b/u2-2419200-25.user.out 2> q5b/u2-2419200-25.user.err 

999:
	mkdir q5b/u2-2419200-26.user.sols;./gjsolver q5b/u2-2419200-26.user q5b/u2-2419200-26.user.sols 1> q5b/u2-2419200-26.user.out 2> q5b/u2-2419200-26.user.err 

1000:
	mkdir q5b/u2-2419200-27.user.sols;./gjsolver q5b/u2-2419200-27.user q5b/u2-2419200-27.user.sols 1> q5b/u2-2419200-27.user.out 2> q5b/u2-2419200-27.user.err 

1001:
	mkdir q5b/u2-2419200-28.user.sols;./gjsolver q5b/u2-2419200-28.user q5b/u2-2419200-28.user.sols 1> q5b/u2-2419200-28.user.out 2> q5b/u2-2419200-28.user.err 

1002:
	mkdir q5b/u2-2419200-29.user.sols;./gjsolver q5b/u2-2419200-29.user q5b/u2-2419200-29.user.sols 1> q5b/u2-2419200-29.user.out 2> q5b/u2-2419200-29.user.err 

1003:
	mkdir q5b/u2-2419200-3.user.sols;./gjsolver q5b/u2-2419200-3.user q5b/u2-2419200-3.user.sols 1> q5b/u2-2419200-3.user.out 2> q5b/u2-2419200-3.user.err 

1004:
	mkdir q5b/u2-2419200-30.user.sols;./gjsolver q5b/u2-2419200-30.user q5b/u2-2419200-30.user.sols 1> q5b/u2-2419200-30.user.out 2> q5b/u2-2419200-30.user.err 

1005:
	mkdir q5b/u2-2419200-4.user.sols;./gjsolver q5b/u2-2419200-4.user q5b/u2-2419200-4.user.sols 1> q5b/u2-2419200-4.user.out 2> q5b/u2-2419200-4.user.err 

1006:
	mkdir q5b/u2-2419200-5.user.sols;./gjsolver q5b/u2-2419200-5.user q5b/u2-2419200-5.user.sols 1> q5b/u2-2419200-5.user.out 2> q5b/u2-2419200-5.user.err 

1007:
	mkdir q5b/u2-2419200-6.user.sols;./gjsolver q5b/u2-2419200-6.user q5b/u2-2419200-6.user.sols 1> q5b/u2-2419200-6.user.out 2> q5b/u2-2419200-6.user.err 

1008:
	mkdir q5b/u2-2419200-7.user.sols;./gjsolver q5b/u2-2419200-7.user q5b/u2-2419200-7.user.sols 1> q5b/u2-2419200-7.user.out 2> q5b/u2-2419200-7.user.err 

1009:
	mkdir q5b/u2-2419200-8.user.sols;./gjsolver q5b/u2-2419200-8.user q5b/u2-2419200-8.user.sols 1> q5b/u2-2419200-8.user.out 2> q5b/u2-2419200-8.user.err 

1010:
	mkdir q5b/u2-2419200-9.user.sols;./gjsolver q5b/u2-2419200-9.user q5b/u2-2419200-9.user.sols 1> q5b/u2-2419200-9.user.out 2> q5b/u2-2419200-9.user.err 

1011:
	mkdir q5b/u2-604800-1.user.sols;./gjsolver q5b/u2-604800-1.user q5b/u2-604800-1.user.sols 1> q5b/u2-604800-1.user.out 2> q5b/u2-604800-1.user.err 

1012:
	mkdir q5b/u2-604800-10.user.sols;./gjsolver q5b/u2-604800-10.user q5b/u2-604800-10.user.sols 1> q5b/u2-604800-10.user.out 2> q5b/u2-604800-10.user.err 

1013:
	mkdir q5b/u2-604800-11.user.sols;./gjsolver q5b/u2-604800-11.user q5b/u2-604800-11.user.sols 1> q5b/u2-604800-11.user.out 2> q5b/u2-604800-11.user.err 

1014:
	mkdir q5b/u2-604800-12.user.sols;./gjsolver q5b/u2-604800-12.user q5b/u2-604800-12.user.sols 1> q5b/u2-604800-12.user.out 2> q5b/u2-604800-12.user.err 

1015:
	mkdir q5b/u2-604800-13.user.sols;./gjsolver q5b/u2-604800-13.user q5b/u2-604800-13.user.sols 1> q5b/u2-604800-13.user.out 2> q5b/u2-604800-13.user.err 

1016:
	mkdir q5b/u2-604800-14.user.sols;./gjsolver q5b/u2-604800-14.user q5b/u2-604800-14.user.sols 1> q5b/u2-604800-14.user.out 2> q5b/u2-604800-14.user.err 

1017:
	mkdir q5b/u2-604800-15.user.sols;./gjsolver q5b/u2-604800-15.user q5b/u2-604800-15.user.sols 1> q5b/u2-604800-15.user.out 2> q5b/u2-604800-15.user.err 

1018:
	mkdir q5b/u2-604800-16.user.sols;./gjsolver q5b/u2-604800-16.user q5b/u2-604800-16.user.sols 1> q5b/u2-604800-16.user.out 2> q5b/u2-604800-16.user.err 

1019:
	mkdir q5b/u2-604800-17.user.sols;./gjsolver q5b/u2-604800-17.user q5b/u2-604800-17.user.sols 1> q5b/u2-604800-17.user.out 2> q5b/u2-604800-17.user.err 

1020:
	mkdir q5b/u2-604800-18.user.sols;./gjsolver q5b/u2-604800-18.user q5b/u2-604800-18.user.sols 1> q5b/u2-604800-18.user.out 2> q5b/u2-604800-18.user.err 

1021:
	mkdir q5b/u2-604800-19.user.sols;./gjsolver q5b/u2-604800-19.user q5b/u2-604800-19.user.sols 1> q5b/u2-604800-19.user.out 2> q5b/u2-604800-19.user.err 

1022:
	mkdir q5b/u2-604800-2.user.sols;./gjsolver q5b/u2-604800-2.user q5b/u2-604800-2.user.sols 1> q5b/u2-604800-2.user.out 2> q5b/u2-604800-2.user.err 

1023:
	mkdir q5b/u2-604800-20.user.sols;./gjsolver q5b/u2-604800-20.user q5b/u2-604800-20.user.sols 1> q5b/u2-604800-20.user.out 2> q5b/u2-604800-20.user.err 

1024:
	mkdir q5b/u2-604800-21.user.sols;./gjsolver q5b/u2-604800-21.user q5b/u2-604800-21.user.sols 1> q5b/u2-604800-21.user.out 2> q5b/u2-604800-21.user.err 

1025:
	mkdir q5b/u2-604800-22.user.sols;./gjsolver q5b/u2-604800-22.user q5b/u2-604800-22.user.sols 1> q5b/u2-604800-22.user.out 2> q5b/u2-604800-22.user.err 

1026:
	mkdir q5b/u2-604800-23.user.sols;./gjsolver q5b/u2-604800-23.user q5b/u2-604800-23.user.sols 1> q5b/u2-604800-23.user.out 2> q5b/u2-604800-23.user.err 

1027:
	mkdir q5b/u2-604800-24.user.sols;./gjsolver q5b/u2-604800-24.user q5b/u2-604800-24.user.sols 1> q5b/u2-604800-24.user.out 2> q5b/u2-604800-24.user.err 

1028:
	mkdir q5b/u2-604800-25.user.sols;./gjsolver q5b/u2-604800-25.user q5b/u2-604800-25.user.sols 1> q5b/u2-604800-25.user.out 2> q5b/u2-604800-25.user.err 

1029:
	mkdir q5b/u2-604800-26.user.sols;./gjsolver q5b/u2-604800-26.user q5b/u2-604800-26.user.sols 1> q5b/u2-604800-26.user.out 2> q5b/u2-604800-26.user.err 

1030:
	mkdir q5b/u2-604800-27.user.sols;./gjsolver q5b/u2-604800-27.user q5b/u2-604800-27.user.sols 1> q5b/u2-604800-27.user.out 2> q5b/u2-604800-27.user.err 

1031:
	mkdir q5b/u2-604800-28.user.sols;./gjsolver q5b/u2-604800-28.user q5b/u2-604800-28.user.sols 1> q5b/u2-604800-28.user.out 2> q5b/u2-604800-28.user.err 

1032:
	mkdir q5b/u2-604800-29.user.sols;./gjsolver q5b/u2-604800-29.user q5b/u2-604800-29.user.sols 1> q5b/u2-604800-29.user.out 2> q5b/u2-604800-29.user.err 

1033:
	mkdir q5b/u2-604800-3.user.sols;./gjsolver q5b/u2-604800-3.user q5b/u2-604800-3.user.sols 1> q5b/u2-604800-3.user.out 2> q5b/u2-604800-3.user.err 

1034:
	mkdir q5b/u2-604800-30.user.sols;./gjsolver q5b/u2-604800-30.user q5b/u2-604800-30.user.sols 1> q5b/u2-604800-30.user.out 2> q5b/u2-604800-30.user.err 

1035:
	mkdir q5b/u2-604800-4.user.sols;./gjsolver q5b/u2-604800-4.user q5b/u2-604800-4.user.sols 1> q5b/u2-604800-4.user.out 2> q5b/u2-604800-4.user.err 

1036:
	mkdir q5b/u2-604800-5.user.sols;./gjsolver q5b/u2-604800-5.user q5b/u2-604800-5.user.sols 1> q5b/u2-604800-5.user.out 2> q5b/u2-604800-5.user.err 

1037:
	mkdir q5b/u2-604800-6.user.sols;./gjsolver q5b/u2-604800-6.user q5b/u2-604800-6.user.sols 1> q5b/u2-604800-6.user.out 2> q5b/u2-604800-6.user.err 

1038:
	mkdir q5b/u2-604800-7.user.sols;./gjsolver q5b/u2-604800-7.user q5b/u2-604800-7.user.sols 1> q5b/u2-604800-7.user.out 2> q5b/u2-604800-7.user.err 

1039:
	mkdir q5b/u2-604800-8.user.sols;./gjsolver q5b/u2-604800-8.user q5b/u2-604800-8.user.sols 1> q5b/u2-604800-8.user.out 2> q5b/u2-604800-8.user.err 

1040:
	mkdir q5b/u2-604800-9.user.sols;./gjsolver q5b/u2-604800-9.user q5b/u2-604800-9.user.sols 1> q5b/u2-604800-9.user.out 2> q5b/u2-604800-9.user.err 

