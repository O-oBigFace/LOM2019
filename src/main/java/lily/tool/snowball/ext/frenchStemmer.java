// This file was generated automatically by the Snowball to Java compiler

package lily.tool.snowball.ext;
import lily.tool.snowball.SnowballProgram;
import lily.tool.snowball.Among;

/**
 * Generated class implementing code defined by a snowball script.
 */
public class frenchStemmer extends SnowballProgram {

        private Among a_0[] = {
            new Among ( "col", -1, -1, "", this),
            new Among ( "par", -1, -1, "", this),
            new Among ( "tap", -1, -1, "", this)
        };

        private Among a_1[] = {
            new Among ( "", -1, 4, "", this),
            new Among ( "I", 0, 1, "", this),
            new Among ( "U", 0, 2, "", this),
            new Among ( "Y", 0, 3, "", this)
        };

        private Among a_2[] = {
            new Among ( "iqU", -1, 3, "", this),
            new Among ( "abl", -1, 3, "", this),
            new Among ( "I\u00E8r", -1, 4, "", this),
            new Among ( "i\u00E8r", -1, 4, "", this),
            new Among ( "eus", -1, 2, "", this),
            new Among ( "iv", -1, 1, "", this)
        };

        private Among a_3[] = {
            new Among ( "ic", -1, 2, "", this),
            new Among ( "abil", -1, 1, "", this),
            new Among ( "iv", -1, 3, "", this)
        };

        private Among a_4[] = {
            new Among ( "iqUe", -1, 1, "", this),
            new Among ( "atrice", -1, 2, "", this),
            new Among ( "ance", -1, 1, "", this),
            new Among ( "ence", -1, 5, "", this),
            new Among ( "logie", -1, 3, "", this),
            new Among ( "able", -1, 1, "", this),
            new Among ( "isme", -1, 1, "", this),
            new Among ( "euse", -1, 11, "", this),
            new Among ( "iste", -1, 1, "", this),
            new Among ( "ive", -1, 8, "", this),
            new Among ( "if", -1, 8, "", this),
            new Among ( "usion", -1, 4, "", this),
            new Among ( "ation", -1, 2, "", this),
            new Among ( "ution", -1, 4, "", this),
            new Among ( "ateur", -1, 2, "", this),
            new Among ( "iqUes", -1, 1, "", this),
            new Among ( "atrices", -1, 2, "", this),
            new Among ( "ances", -1, 1, "", this),
            new Among ( "ences", -1, 5, "", this),
            new Among ( "logies", -1, 3, "", this),
            new Among ( "ables", -1, 1, "", this),
            new Among ( "ismes", -1, 1, "", this),
            new Among ( "euses", -1, 11, "", this),
            new Among ( "istes", -1, 1, "", this),
            new Among ( "ives", -1, 8, "", this),
            new Among ( "ifs", -1, 8, "", this),
            new Among ( "usions", -1, 4, "", this),
            new Among ( "ations", -1, 2, "", this),
            new Among ( "utions", -1, 4, "", this),
            new Among ( "ateurs", -1, 2, "", this),
            new Among ( "ments", -1, 15, "", this),
            new Among ( "ements", 30, 6, "", this),
            new Among ( "issements", 31, 12, "", this),
            new Among ( "it\u00E9s", -1, 7, "", this),
            new Among ( "ment", -1, 15, "", this),
            new Among ( "ement", 34, 6, "", this),
            new Among ( "issement", 35, 12, "", this),
            new Among ( "amment", 34, 13, "", this),
            new Among ( "emment", 34, 14, "", this),
            new Among ( "aux", -1, 10, "", this),
            new Among ( "eaux", 39, 9, "", this),
            new Among ( "eux", -1, 1, "", this),
            new Among ( "it\u00E9", -1, 7, "", this)
        };

        private Among a_5[] = {
            new Among ( "ira", -1, 1, "", this),
            new Among ( "ie", -1, 1, "", this),
            new Among ( "isse", -1, 1, "", this),
            new Among ( "issante", -1, 1, "", this),
            new Among ( "i", -1, 1, "", this),
            new Among ( "irai", 4, 1, "", this),
            new Among ( "ir", -1, 1, "", this),
            new Among ( "iras", -1, 1, "", this),
            new Among ( "ies", -1, 1, "", this),
            new Among ( "\u00EEmes", -1, 1, "", this),
            new Among ( "isses", -1, 1, "", this),
            new Among ( "issantes", -1, 1, "", this),
            new Among ( "\u00EEtes", -1, 1, "", this),
            new Among ( "is", -1, 1, "", this),
            new Among ( "irais", 13, 1, "", this),
            new Among ( "issais", 13, 1, "", this),
            new Among ( "irions", -1, 1, "", this),
            new Among ( "issions", -1, 1, "", this),
            new Among ( "irons", -1, 1, "", this),
            new Among ( "issons", -1, 1, "", this),
            new Among ( "issants", -1, 1, "", this),
            new Among ( "it", -1, 1, "", this),
            new Among ( "irait", 21, 1, "", this),
            new Among ( "issait", 21, 1, "", this),
            new Among ( "issant", -1, 1, "", this),
            new Among ( "iraIent", -1, 1, "", this),
            new Among ( "issaIent", -1, 1, "", this),
            new Among ( "irent", -1, 1, "", this),
            new Among ( "issent", -1, 1, "", this),
            new Among ( "iront", -1, 1, "", this),
            new Among ( "\u00EEt", -1, 1, "", this),
            new Among ( "iriez", -1, 1, "", this),
            new Among ( "issiez", -1, 1, "", this),
            new Among ( "irez", -1, 1, "", this),
            new Among ( "issez", -1, 1, "", this)
        };

        private Among a_6[] = {
            new Among ( "a", -1, 3, "", this),
            new Among ( "era", 0, 2, "", this),
            new Among ( "asse", -1, 3, "", this),
            new Among ( "ante", -1, 3, "", this),
            new Among ( "\u00E9e", -1, 2, "", this),
            new Among ( "ai", -1, 3, "", this),
            new Among ( "erai", 5, 2, "", this),
            new Among ( "er", -1, 2, "", this),
            new Among ( "as", -1, 3, "", this),
            new Among ( "eras", 8, 2, "", this),
            new Among ( "\u00E2mes", -1, 3, "", this),
            new Among ( "asses", -1, 3, "", this),
            new Among ( "antes", -1, 3, "", this),
            new Among ( "\u00E2tes", -1, 3, "", this),
            new Among ( "\u00E9es", -1, 2, "", this),
            new Among ( "ais", -1, 3, "", this),
            new Among ( "erais", 15, 2, "", this),
            new Among ( "ions", -1, 1, "", this),
            new Among ( "erions", 17, 2, "", this),
            new Among ( "assions", 17, 3, "", this),
            new Among ( "erons", -1, 2, "", this),
            new Among ( "ants", -1, 3, "", this),
            new Among ( "\u00E9s", -1, 2, "", this),
            new Among ( "ait", -1, 3, "", this),
            new Among ( "erait", 23, 2, "", this),
            new Among ( "ant", -1, 3, "", this),
            new Among ( "aIent", -1, 3, "", this),
            new Among ( "eraIent", 26, 2, "", this),
            new Among ( "\u00E8rent", -1, 2, "", this),
            new Among ( "assent", -1, 3, "", this),
            new Among ( "eront", -1, 2, "", this),
            new Among ( "\u00E2t", -1, 3, "", this),
            new Among ( "ez", -1, 2, "", this),
            new Among ( "iez", 32, 2, "", this),
            new Among ( "eriez", 33, 2, "", this),
            new Among ( "assiez", 33, 3, "", this),
            new Among ( "erez", 32, 2, "", this),
            new Among ( "\u00E9", -1, 2, "", this)
        };

        private Among a_7[] = {
            new Among ( "e", -1, 3, "", this),
            new Among ( "I\u00E8re", 0, 2, "", this),
            new Among ( "i\u00E8re", 0, 2, "", this),
            new Among ( "ion", -1, 1, "", this),
            new Among ( "Ier", -1, 2, "", this),
            new Among ( "ier", -1, 2, "", this),
            new Among ( "\u00EB", -1, 4, "", this)
        };

        private Among a_8[] = {
            new Among ( "ell", -1, -1, "", this),
            new Among ( "eill", -1, -1, "", this),
            new Among ( "enn", -1, -1, "", this),
            new Among ( "onn", -1, -1, "", this),
            new Among ( "ett", -1, -1, "", this)
        };

        private static final char g_v[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 130, 103, 8, 5 };

        private static final char g_keep_with_s[] = {1, 65, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128 };

        private int I_p2;
        private int I_p1;
        private int I_pV;

        private void copy_from(frenchStemmer other) {
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            I_pV = other.I_pV;
            super.copy_from(other);
        }

        private boolean r_prelude() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            // repeat, line 38
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    // goto, line 38
                    golab2: while(true)
                    {
                        v_2 = cursor;
                        lab3: do {
                            // (, line 38
                            // or, line 44
                            lab4: do {
                                v_3 = cursor;
                                lab5: do {
                                    // (, line 40
                                    if (!(in_grouping(g_v, 97, 251)))
                                    {
                                        break lab5;
                                    }
                                    // [, line 40
                                    bra = cursor;
                                    // or, line 40
                                    lab6: do {
                                        v_4 = cursor;
                                        lab7: do {
                                            // (, line 40
                                            // literal, line 40
                                            if (!(eq_s(1, "u")))
                                            {
                                                break lab7;
                                            }
                                            // ], line 40
                                            ket = cursor;
                                            if (!(in_grouping(g_v, 97, 251)))
                                            {
                                                break lab7;
                                            }
                                            // <-, line 40
                                            slice_from("U");
                                            break lab6;
                                        } while (false);
                                        cursor = v_4;
                                        lab8: do {
                                            // (, line 41
                                            // literal, line 41
                                            if (!(eq_s(1, "i")))
                                            {
                                                break lab8;
                                            }
                                            // ], line 41
                                            ket = cursor;
                                            if (!(in_grouping(g_v, 97, 251)))
                                            {
                                                break lab8;
                                            }
                                            // <-, line 41
                                            slice_from("I");
                                            break lab6;
                                        } while (false);
                                        cursor = v_4;
                                        // (, line 42
                                        // literal, line 42
                                        if (!(eq_s(1, "y")))
                                        {
                                            break lab5;
                                        }
                                        // ], line 42
                                        ket = cursor;
                                        // <-, line 42
                                        slice_from("Y");
                                    } while (false);
                                    break lab4;
                                } while (false);
                                cursor = v_3;
                                lab9: do {
                                    // (, line 45
                                    // [, line 45
                                    bra = cursor;
                                    // literal, line 45
                                    if (!(eq_s(1, "y")))
                                    {
                                        break lab9;
                                    }
                                    // ], line 45
                                    ket = cursor;
                                    if (!(in_grouping(g_v, 97, 251)))
                                    {
                                        break lab9;
                                    }
                                    // <-, line 45
                                    slice_from("Y");
                                    break lab4;
                                } while (false);
                                cursor = v_3;
                                // (, line 47
                                // literal, line 47
                                if (!(eq_s(1, "q")))
                                {
                                    break lab3;
                                }
                                // [, line 47
                                bra = cursor;
                                // literal, line 47
                                if (!(eq_s(1, "u")))
                                {
                                    break lab3;
                                }
                                // ], line 47
                                ket = cursor;
                                // <-, line 47
                                slice_from("U");
                            } while (false);
                            cursor = v_2;
                            break golab2;
                        } while (false);
                        cursor = v_2;
                        if (cursor >= limit)
                        {
                            break lab1;
                        }
                        cursor++;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }

        private boolean r_mark_regions() {
            int v_1;
            int v_2;
            int v_4;
            // (, line 50
            I_pV = limit;
            I_p1 = limit;
            I_p2 = limit;
            // do, line 56
            v_1 = cursor;
            lab0: do {
                // (, line 56
                // or, line 58
                lab1: do {
                    v_2 = cursor;
                    lab2: do {
                        // (, line 57
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab2;
                        }
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab2;
                        }
                        // next, line 57
                        if (cursor >= limit)
                        {
                            break lab2;
                        }
                        cursor++;
                        break lab1;
                    } while (false);
                    cursor = v_2;
                    lab3: do {
                        // among, line 59
                        if (find_among(a_0, 3) == 0)
                        {
                            break lab3;
                        }
                        break lab1;
                    } while (false);
                    cursor = v_2;
                    // (, line 66
                    // next, line 66
                    if (cursor >= limit)
                    {
                        break lab0;
                    }
                    cursor++;
                    // gopast, line 66
                    golab4: while(true)
                    {
                        lab5: do {
                            if (!(in_grouping(g_v, 97, 251)))
                            {
                                break lab5;
                            }
                            break golab4;
                        } while (false);
                        if (cursor >= limit)
                        {
                            break lab0;
                        }
                        cursor++;
                    }
                } while (false);
                // setmark pV, line 67
                I_pV = cursor;
            } while (false);
            cursor = v_1;
            // do, line 69
            v_4 = cursor;
            lab6: do {
                // (, line 69
                // gopast, line 70
                golab7: while(true)
                {
                    lab8: do {
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab8;
                        }
                        break golab7;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // gopast, line 70
                golab9: while(true)
                {
                    lab10: do {
                        if (!(out_grouping(g_v, 97, 251)))
                        {
                            break lab10;
                        }
                        break golab9;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // setmark p1, line 70
                I_p1 = cursor;
                // gopast, line 71
                golab11: while(true)
                {
                    lab12: do {
                        if (!(in_grouping(g_v, 97, 251)))
                        {
                            break lab12;
                        }
                        break golab11;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // gopast, line 71
                golab13: while(true)
                {
                    lab14: do {
                        if (!(out_grouping(g_v, 97, 251)))
                        {
                            break lab14;
                        }
                        break golab13;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab6;
                    }
                    cursor++;
                }
                // setmark p2, line 71
                I_p2 = cursor;
            } while (false);
            cursor = v_4;
            return true;
        }

        private boolean r_postlude() {
            int among_var;
            int v_1;
            // repeat, line 75
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    // (, line 75
                    // [, line 77
                    bra = cursor;
                    // substring, line 77
                    among_var = find_among(a_1, 4);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    // ], line 77
                    ket = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            // (, line 78
                            // <-, line 78
                            slice_from("i");
                            break;
                        case 2:
                            // (, line 79
                            // <-, line 79
                            slice_from("u");
                            break;
                        case 3:
                            // (, line 80
                            // <-, line 80
                            slice_from("y");
                            break;
                        case 4:
                            // (, line 81
                            // next, line 81
                            if (cursor >= limit)
                            {
                                break lab1;
                            }
                            cursor++;
                            break;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }

        private boolean r_RV() {
            if (!(I_pV <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_R1() {
            if (!(I_p1 <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }

        private boolean r_standard_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            int v_10;
            int v_11;
            // (, line 91
            // [, line 92
            ket = cursor;
            // substring, line 92
            among_var = find_among_b(a_4, 43);
            if (among_var == 0)
            {
                return false;
            }
            // ], line 92
            bra = cursor;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    // (, line 96
                    // call R2, line 96
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 96
                    slice_del();
                    break;
                case 2:
                    // (, line 99
                    // call R2, line 99
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 99
                    slice_del();
                    // try, line 100
                    v_1 = limit - cursor;
                    lab0: do {
                        // (, line 100
                        // [, line 100
                        ket = cursor;
                        // literal, line 100
                        if (!(eq_s_b(2, "ic")))
                        {
                            cursor = limit - v_1;
                            break lab0;
                        }
                        // ], line 100
                        bra = cursor;
                        // or, line 100
                        lab1: do {
                            v_2 = limit - cursor;
                            lab2: do {
                                // (, line 100
                                // call R2, line 100
                                if (!r_R2())
                                {
                                    break lab2;
                                }
                                // delete, line 100
                                slice_del();
                                break lab1;
                            } while (false);
                            cursor = limit - v_2;
                            // <-, line 100
                            slice_from("iqU");
                        } while (false);
                    } while (false);
                    break;
                case 3:
                    // (, line 104
                    // call R2, line 104
                    if (!r_R2())
                    {
                        return false;
                    }
                    // <-, line 104
                    slice_from("log");
                    break;
                case 4:
                    // (, line 107
                    // call R2, line 107
                    if (!r_R2())
                    {
                        return false;
                    }
                    // <-, line 107
                    slice_from("u");
                    break;
                case 5:
                    // (, line 110
                    // call R2, line 110
                    if (!r_R2())
                    {
                        return false;
                    }
                    // <-, line 110
                    slice_from("ent");
                    break;
                case 6:
                    // (, line 113
                    // call RV, line 114
                    if (!r_RV())
                    {
                        return false;
                    }
                    // delete, line 114
                    slice_del();
                    // try, line 115
                    v_3 = limit - cursor;
                    lab3: do {
                        // (, line 115
                        // [, line 116
                        ket = cursor;
                        // substring, line 116
                        among_var = find_among_b(a_2, 6);
                        if (among_var == 0)
                        {
                            cursor = limit - v_3;
                            break lab3;
                        }
                        // ], line 116
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_3;
                                break lab3;
                            case 1:
                                // (, line 117
                                // call R2, line 117
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // delete, line 117
                                slice_del();
                                // [, line 117
                                ket = cursor;
                                // literal, line 117
                                if (!(eq_s_b(2, "at")))
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // ], line 117
                                bra = cursor;
                                // call R2, line 117
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // delete, line 117
                                slice_del();
                                break;
                            case 2:
                                // (, line 118
                                // or, line 118
                                lab4: do {
                                    v_4 = limit - cursor;
                                    lab5: do {
                                        // (, line 118
                                        // call R2, line 118
                                        if (!r_R2())
                                        {
                                            break lab5;
                                        }
                                        // delete, line 118
                                        slice_del();
                                        break lab4;
                                    } while (false);
                                    cursor = limit - v_4;
                                    // (, line 118
                                    // call R1, line 118
                                    if (!r_R1())
                                    {
                                        cursor = limit - v_3;
                                        break lab3;
                                    }
                                    // <-, line 118
                                    slice_from("eux");
                                } while (false);
                                break;
                            case 3:
                                // (, line 120
                                // call R2, line 120
                                if (!r_R2())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // delete, line 120
                                slice_del();
                                break;
                            case 4:
                                // (, line 122
                                // call RV, line 122
                                if (!r_RV())
                                {
                                    cursor = limit - v_3;
                                    break lab3;
                                }
                                // <-, line 122
                                slice_from("i");
                                break;
                        }
                    } while (false);
                    break;
                case 7:
                    // (, line 128
                    // call R2, line 129
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 129
                    slice_del();
                    // try, line 130
                    v_5 = limit - cursor;
                    lab6: do {
                        // (, line 130
                        // [, line 131
                        ket = cursor;
                        // substring, line 131
                        among_var = find_among_b(a_3, 3);
                        if (among_var == 0)
                        {
                            cursor = limit - v_5;
                            break lab6;
                        }
                        // ], line 131
                        bra = cursor;
                        switch(among_var) {
                            case 0:
                                cursor = limit - v_5;
                                break lab6;
                            case 1:
                                // (, line 132
                                // or, line 132
                                lab7: do {
                                    v_6 = limit - cursor;
                                    lab8: do {
                                        // (, line 132
                                        // call R2, line 132
                                        if (!r_R2())
                                        {
                                            break lab8;
                                        }
                                        // delete, line 132
                                        slice_del();
                                        break lab7;
                                    } while (false);
                                    cursor = limit - v_6;
                                    // <-, line 132
                                    slice_from("abl");
                                } while (false);
                                break;
                            case 2:
                                // (, line 133
                                // or, line 133
                                lab9: do {
                                    v_7 = limit - cursor;
                                    lab10: do {
                                        // (, line 133
                                        // call R2, line 133
                                        if (!r_R2())
                                        {
                                            break lab10;
                                        }
                                        // delete, line 133
                                        slice_del();
                                        break lab9;
                                    } while (false);
                                    cursor = limit - v_7;
                                    // <-, line 133
                                    slice_from("iqU");
                                } while (false);
                                break;
                            case 3:
                                // (, line 134
                                // call R2, line 134
                                if (!r_R2())
                                {
                                    cursor = limit - v_5;
                                    break lab6;
                                }
                                // delete, line 134
                                slice_del();
                                break;
                        }
                    } while (false);
                    break;
                case 8:
                    // (, line 140
                    // call R2, line 141
                    if (!r_R2())
                    {
                        return false;
                    }
                    // delete, line 141
                    slice_del();
                    // try, line 142
                    v_8 = limit - cursor;
                    lab11: do {
                        // (, line 142
                        // [, line 142
                        ket = cursor;
                        // literal, line 142
                        if (!(eq_s_b(2, "at")))
                        {
                            cursor = limit - v_8;
                            break lab11;
                        }
                        // ], line 142
                        bra = cursor;
                        // call R2, line 142
                        if (!r_R2())
                        {
                            cursor = limit - v_8;
                            break lab11;
                        }
                        // delete, line 142
                        slice_del();
                        // [, line 142
                        ket = cursor;
                        // literal, line 142
                        if (!(eq_s_b(2, "ic")))
                        {
                            cursor = limit - v_8;
                            break lab11;
                        }
                        // ], line 142
                        bra = cursor;
                        // or, line 142
                        lab12: do {
                            v_9 = limit - cursor;
                            lab13: do {
                                // (, line 142
                                // call R2, line 142
                                if (!r_R2())
                                {
                                    break lab13;
                                }
                                // delete, line 142
                                slice_del();
                                break lab12;
                            } while (false);
                            cursor = limit - v_9;
                            // <-, line 142
                            slice_from("iqU");
                        } while (false);
                    } while (false);
                    break;
                case 9:
                    // (, line 144
                    // <-, line 144
                    slice_from("eau");
                    break;
                case 10:
                    // (, line 145
                    // call R1, line 145
                    if (!r_R1())
                    {
                        return false;
                    }
                    // <-, line 145
                    slice_from("al");
                    break;
                case 11:
                    // (, line 147
                    // or, line 147
                    lab14: do {
                        v_10 = limit - cursor;
                        lab15: do {
                            // (, line 147
                            // call R2, line 147
                            if (!r_R2())
                            {
                                break lab15;
                            }
                            // delete, line 147
                            slice_del();
                            break lab14;
                        } while (false);
                        cursor = limit - v_10;
                        // (, line 147
                        // call R1, line 147
                        if (!r_R1())
                        {
                            return false;
                        }
                        // <-, line 147
                        slice_from("eux");
                    } while (false);
                    break;
                case 12:
                    // (, line 150
                    // call R1, line 150
                    if (!r_R1())
                    {
                        return false;
                    }
                    if (!(out_grouping_b(g_v, 97, 251)))
                    {
                        return false;
                    }
                    // delete, line 150
                    slice_del();
                    break;
                case 13:
                    // (, line 155
                    // call RV, line 155
                    if (!r_RV())
                    {
                        return false;
                    }
                    // fail, line 155
                    // (, line 155
                    // <-, line 155
                    slice_from("ant");
                    return false;
                case 14:
                    // (, line 156
                    // call RV, line 156
                    if (!r_RV())
                    {
                        return false;
                    }
                    // fail, line 156
                    // (, line 156
                    // <-, line 156
                    slice_from("ent");
                    return false;
                case 15:
                    // (, line 158
                    // test, line 158
                    v_11 = limit - cursor;
                    // (, line 158
                    if (!(in_grouping_b(g_v, 97, 251)))
                    {
                        return false;
                    }
                    // call RV, line 158
                    if (!r_RV())
                    {
                        return false;
                    }
                    cursor = limit - v_11;
                    // fail, line 158
                    // (, line 158
                    // delete, line 158
                    slice_del();
                    return false;
            }
            return true;
        }

        private boolean r_i_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            // setlimit, line 163
            v_1 = limit - cursor;
            // tomark, line 163
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            // (, line 163
            // [, line 164
            ket = cursor;
            // substring, line 164
            among_var = find_among_b(a_5, 35);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            // ], line 164
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_2;
                    return false;
                case 1:
                    // (, line 170
                    if (!(out_grouping_b(g_v, 97, 251)))
                    {
                        limit_backward = v_2;
                        return false;
                    }
                    // delete, line 170
                    slice_del();
                    break;
            }
            limit_backward = v_2;
            return true;
        }

        private boolean r_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            // setlimit, line 174
            v_1 = limit - cursor;
            // tomark, line 174
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            // (, line 174
            // [, line 175
            ket = cursor;
            // substring, line 175
            among_var = find_among_b(a_6, 38);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            // ], line 175
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_2;
                    return false;
                case 1:
                    // (, line 177
                    // call R2, line 177
                    if (!r_R2())
                    {
                        limit_backward = v_2;
                        return false;
                    }
                    // delete, line 177
                    slice_del();
                    break;
                case 2:
                    // (, line 185
                    // delete, line 185
                    slice_del();
                    break;
                case 3:
                    // (, line 190
                    // delete, line 190
                    slice_del();
                    // try, line 191
                    v_3 = limit - cursor;
                    lab0: do {
                        // (, line 191
                        // [, line 191
                        ket = cursor;
                        // literal, line 191
                        if (!(eq_s_b(1, "e")))
                        {
                            cursor = limit - v_3;
                            break lab0;
                        }
                        // ], line 191
                        bra = cursor;
                        // delete, line 191
                        slice_del();
                    } while (false);
                    break;
            }
            limit_backward = v_2;
            return true;
        }

        private boolean r_residual_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            // (, line 198
            // try, line 199
            v_1 = limit - cursor;
            lab0: do {
                // (, line 199
                // [, line 199
                ket = cursor;
                // literal, line 199
                if (!(eq_s_b(1, "s")))
                {
                    cursor = limit - v_1;
                    break lab0;
                }
                // ], line 199
                bra = cursor;
                // test, line 199
                v_2 = limit - cursor;
                if (!(out_grouping_b(g_keep_with_s, 97, 232)))
                {
                    cursor = limit - v_1;
                    break lab0;
                }
                cursor = limit - v_2;
                // delete, line 199
                slice_del();
            } while (false);
            // setlimit, line 200
            v_3 = limit - cursor;
            // tomark, line 200
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_4 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_3;
            // (, line 200
            // [, line 201
            ket = cursor;
            // substring, line 201
            among_var = find_among_b(a_7, 7);
            if (among_var == 0)
            {
                limit_backward = v_4;
                return false;
            }
            // ], line 201
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_4;
                    return false;
                case 1:
                    // (, line 202
                    // call R2, line 202
                    if (!r_R2())
                    {
                        limit_backward = v_4;
                        return false;
                    }
                    // or, line 202
                    lab1: do {
                        v_5 = limit - cursor;
                        lab2: do {
                            // literal, line 202
                            if (!(eq_s_b(1, "s")))
                            {
                                break lab2;
                            }
                            break lab1;
                        } while (false);
                        cursor = limit - v_5;
                        // literal, line 202
                        if (!(eq_s_b(1, "t")))
                        {
                            limit_backward = v_4;
                            return false;
                        }
                    } while (false);
                    // delete, line 202
                    slice_del();
                    break;
                case 2:
                    // (, line 204
                    // <-, line 204
                    slice_from("i");
                    break;
                case 3:
                    // (, line 205
                    // delete, line 205
                    slice_del();
                    break;
                case 4:
                    // (, line 206
                    // literal, line 206
                    if (!(eq_s_b(2, "gu")))
                    {
                        limit_backward = v_4;
                        return false;
                    }
                    // delete, line 206
                    slice_del();
                    break;
            }
            limit_backward = v_4;
            return true;
        }

        private boolean r_un_double() {
            int v_1;
            // (, line 211
            // test, line 212
            v_1 = limit - cursor;
            // among, line 212
            if (find_among_b(a_8, 5) == 0)
            {
                return false;
            }
            cursor = limit - v_1;
            // [, line 212
            ket = cursor;
            // next, line 212
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            // ], line 212
            bra = cursor;
            // delete, line 212
            slice_del();
            return true;
        }

        private boolean r_un_accent() {
            int v_3;
            // (, line 215
            // atleast, line 216
            {
                int v_1 = 1;
                // atleast, line 216
                replab0: while(true)
                {
                    lab1: do {
                        if (!(out_grouping_b(g_v, 97, 251)))
                        {
                            break lab1;
                        }
                        v_1--;
                        continue replab0;
                    } while (false);
                    break replab0;
                }
                if (v_1 > 0)
                {
                    return false;
                }
            }
            // [, line 217
            ket = cursor;
            // or, line 217
            lab2: do {
                v_3 = limit - cursor;
                lab3: do {
                    // literal, line 217
                    if (!(eq_s_b(1, "\u00E9")))
                    {
                        break lab3;
                    }
                    break lab2;
                } while (false);
                cursor = limit - v_3;
                // literal, line 217
                if (!(eq_s_b(1, "\u00E8")))
                {
                    return false;
                }
            } while (false);
            // ], line 217
            bra = cursor;
            // <-, line 217
            slice_from("e");
            return true;
        }

        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            int v_9;
            int v_10;
            int v_11;
            // (, line 221
            // do, line 223
            v_1 = cursor;
            lab0: do {
                // call prelude, line 223
                if (!r_prelude())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            // do, line 224
            v_2 = cursor;
            lab1: do {
                // call mark_regions, line 224
                if (!r_mark_regions())
                {
                    break lab1;
                }
            } while (false);
            cursor = v_2;
            // backwards, line 225
            limit_backward = cursor; cursor = limit;
            // (, line 225
            // do, line 227
            v_3 = limit - cursor;
            lab2: do {
                // (, line 227
                // or, line 237
                lab3: do {
                    v_4 = limit - cursor;
                    lab4: do {
                        // (, line 228
                        // and, line 233
                        v_5 = limit - cursor;
                        // (, line 229
                        // or, line 229
                        lab5: do {
                            v_6 = limit - cursor;
                            lab6: do {
                                // call standard_suffix, line 229
                                if (!r_standard_suffix())
                                {
                                    break lab6;
                                }
                                break lab5;
                            } while (false);
                            cursor = limit - v_6;
                            lab7: do {
                                // call i_verb_suffix, line 230
                                if (!r_i_verb_suffix())
                                {
                                    break lab7;
                                }
                                break lab5;
                            } while (false);
                            cursor = limit - v_6;
                            // call verb_suffix, line 231
                            if (!r_verb_suffix())
                            {
                                break lab4;
                            }
                        } while (false);
                        cursor = limit - v_5;
                        // try, line 234
                        v_7 = limit - cursor;
                        lab8: do {
                            // (, line 234
                            // [, line 234
                            ket = cursor;
                            // or, line 234
                            lab9: do {
                                v_8 = limit - cursor;
                                lab10: do {
                                    // (, line 234
                                    // literal, line 234
                                    if (!(eq_s_b(1, "Y")))
                                    {
                                        break lab10;
                                    }
                                    // ], line 234
                                    bra = cursor;
                                    // <-, line 234
                                    slice_from("i");
                                    break lab9;
                                } while (false);
                                cursor = limit - v_8;
                                // (, line 235
                                // literal, line 235
                                if (!(eq_s_b(1, "\u00E7")))
                                {
                                    cursor = limit - v_7;
                                    break lab8;
                                }
                                // ], line 235
                                bra = cursor;
                                // <-, line 235
                                slice_from("c");
                            } while (false);
                        } while (false);
                        break lab3;
                    } while (false);
                    cursor = limit - v_4;
                    // call residual_suffix, line 238
                    if (!r_residual_suffix())
                    {
                        break lab2;
                    }
                } while (false);
            } while (false);
            cursor = limit - v_3;
            // do, line 243
            v_9 = limit - cursor;
            lab11: do {
                // call un_double, line 243
                if (!r_un_double())
                {
                    break lab11;
                }
            } while (false);
            cursor = limit - v_9;
            // do, line 244
            v_10 = limit - cursor;
            lab12: do {
                // call un_accent, line 244
                if (!r_un_accent())
                {
                    break lab12;
                }
            } while (false);
            cursor = limit - v_10;
            cursor = limit_backward;            // do, line 246
            v_11 = cursor;
            lab13: do {
                // call postlude, line 246
                if (!r_postlude())
                {
                    break lab13;
                }
            } while (false);
            cursor = v_11;
            return true;
        }

}

