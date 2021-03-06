// fix Keithley2600 to use solution from [math] ??

/*
 * This software was developed at the National Institute of Standards and
 * Technology by a guest researcher in the course of his official duties and
 * with the partial support of the Swiss National Science Foundation. Pursuant
 * to title 17 Section 105 of the United States Code this software is not
 * subject to copyright protection and is in the public domain. The
 * Instrument-Control (iC) software is an experimental system. Neither NIST, nor
 * the Swiss National Science Foundation nor any of the authors assumes any
 * responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or any
 * other characteristic. We would appreciate your citation if the software
 * is used: http://dx.doi.org/10.6028/jres.117.010 .
 *
 * This software can be redistributed and/or modified freely under the terms of
 * the GNU Public Licence and provided that any derivative works bear some
 * notice that they are derived from it, and any modified versions bear some
 * notice that they have been modified.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Public License for more details. http://www.fsf.org
 *
 * This software relies on other open source projects; please see the accompanying
 * _ReadMe_iC.txt for a list of included packages. Thank's very much to those
 * developers !! Without your effort, iC would not have been possible!
 *
 */
package icontrol.drivers.instruments.keithley;

import icontrol.IcontrolTests;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.text.DecimalFormat;
import icontrol.iC_ChartXY.SeriesIdentification;
import icontrol.iC_ChartXY;
import icontrol.IcontrolAppMockup;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Keithley 2600 class.
 * 
 * @author KPP (Kurt Pernstich: pernstich@alumni.ethz.ch)
 * @version 0.1
 *
 */
public class Keithley2600Test extends IcontrolTests {
    
    public Keithley2600Test() {
    }   

    
    @Before
    public void setUp() {
        //System.out.println("in setUp");
    }
    
    @After
    public void tearDown() {
        //System.out.println("in tearDown");
    }
    
        
    /**
     * Uses measured data to test the algorithm used to evaluate the OPVs.
     * @throws IOException Bubbles up from <code>SaveAsPNG</code>
     */
    @Test //(timeout=6*1000+1000)
    public void EvaluateOPV() 
           throws IOException {
        
        // NOTE: when adding more double[][] test data, also add to asList() below
        // and allow an extra second for the test to complete !!
        
        // James' device
        // works with Loess Bandwidth of 0.1 and gives a decent smooth curve
        // <editor-fold defaultstate="collapsed" desc="James' data">
        double[][] James = 
            {{-200.00000E-3,	-319.54700E-6},{-190.00000E-3,	-319.92200E-6},{-180.00000E-3,	-320.65200E-6},{-170.00000E-3,	-320.22600E-6},
            {-160.00000E-3,	-320.09100E-6},{-150.00000E-3,	-317.16900E-6},{-140.00000E-3,	-317.40900E-6},{-130.00000E-3,	-317.86200E-6},
            {-120.00000E-3,	-316.59500E-6},{-110.00000E-3,	-315.53500E-6},{-100.00000E-3,	-317.06200E-6},{-90.00000E-3,	-316.48900E-6},
            {-80.00000E-3,	-317.85100E-6},{-70.00000E-3,	-316.29600E-6},{-60.00000E-3,	-314.85000E-6},{-50.00000E-3,	-314.23400E-6},
            {-40.00000E-3,	-312.21500E-6},{-30.00000E-3,	-311.86100E-6},{-20.00000E-3,	-314.31500E-6},{-10.00000E-3,	-311.90000E-6},
            {-55.51120E-18,	-310.80200E-6},{10.00000E-3,	-311.15700E-6},{20.00000E-3,	-310.63300E-6},{30.00000E-3,	-307.86300E-6},
            {40.00000E-3,	-307.38300E-6},{50.00000E-3,	-306.52000E-6},{60.00000E-3,	-305.20300E-6},{70.00000E-3,	-303.80100E-6},
            {80.00000E-3,	-303.24100E-6},{90.00000E-3,	-304.20800E-6},{100.00000E-3,	-303.40600E-6},{110.00000E-3,	-302.95100E-6},
            {120.00000E-3,	-301.69700E-6},{130.00000E-3,	-299.11400E-6},{140.00000E-3,	-296.86000E-6},{150.00000E-3,	-293.40000E-6},
            {160.00000E-3,	-292.09200E-6},{170.00000E-3,	-289.80600E-6},{180.00000E-3,	-289.82700E-6},{190.00000E-3,	-289.17900E-6},
            {200.00000E-3,	-286.05000E-6},{210.00000E-3,	-284.92700E-6},{220.00000E-3,	-281.98600E-6},{230.00000E-3,	-278.48600E-6},
            {240.00000E-3,	-274.80700E-6},{250.00000E-3,	-274.20400E-6},{260.00000E-3,	-269.39300E-6},{270.00000E-3,	-267.09200E-6},
            {280.00000E-3,	-264.86100E-6},{290.00000E-3,	-260.22300E-6},{300.00000E-3,	-255.75700E-6},{310.00000E-3,	-251.51900E-6},
            {320.00000E-3,	-247.52200E-6},{330.00000E-3,	-242.14400E-6},{340.00000E-3,	-236.57100E-6},{350.00000E-3,	-230.75100E-6},
            {360.00000E-3,	-225.83100E-6},{370.00000E-3,	-219.39800E-6},{380.00000E-3,	-213.07400E-6},{390.00000E-3,	-205.17300E-6},
            {400.00000E-3,	-198.26500E-6},{410.00000E-3,	-190.30700E-6},{420.00000E-3,	-181.70900E-6},{430.00000E-3,	-174.27200E-6},
            {440.00000E-3,	-165.85100E-6},{450.00000E-3,	-156.88200E-6},{460.00000E-3,	-147.86400E-6},{470.00000E-3,	-137.48200E-6},
            {480.00000E-3,	-127.73300E-6},{490.00000E-3,	-117.40400E-6},{500.00000E-3,	-106.25700E-6},{510.00000E-3,	-94.81660E-6},
            {520.00000E-3,	-83.07180E-6},{530.00000E-3,	-71.77530E-6},{540.00000E-3,	-59.10770E-6},{550.00000E-3,	-46.47390E-6},
            {560.00000E-3,	-33.29240E-6},{570.00000E-3,	-19.72630E-6},{580.00000E-3,	-5.28516E-6},{590.00000E-3,	9.52266E-6},
            {600.00000E-3,	24.76310E-6},{610.00000E-3,	40.81530E-6},{620.00000E-3,	57.42350E-6},{630.00000E-3,	74.78120E-6},
            {640.00000E-3,	93.64270E-6},{650.00000E-3,	112.86400E-6},{660.00000E-3,	133.00800E-6},{670.00000E-3,	153.67100E-6},
            {680.00000E-3,	175.33600E-6},{690.00000E-3,	198.27500E-6},{700.00000E-3,	222.05400E-6}};//</editor-fold>
        
        
        // Wei's device
        // only works with Loess Bandwidth of 0.01 and gives not such a good smoothed interpolation
        // <editor-fold defaultstate="collapsed" desc="Wei's data">
        double[][] Wei = {{-1.0	,-2.78598E-5}, {-0.99	,-2.77114E-5}, {-0.98	,-2.7448E-5	}, {-0.97	,-2.74486E-5}, {-0.96	,-2.73369E-5}, {-0.95	
            ,-2.73513E-5}, {-0.94,-2.72033E-5}, {-0.93	,-2.71084E-5}, {-0.92	,-2.69713E-5}, {-0.91	,-2.69542E-5}, {-0.9	
            ,-2.68521E-5}, {-0.89,-2.67774E-5}, {-0.88	,-2.65429E-5}, {-0.87	,-2.63579E-5}, {-0.86	,-2.60967E-5}, {-0.85	
            ,-2.58411E-5}, {-0.84,-2.56843E-5}, {-0.83	,-2.56284E-5}, {-0.82	,-2.55657E-5}, {-0.81	,-2.56038E-5}, {-0.8	
            ,-2.55192E-5}, {-0.79,-2.54179E-5}, {-0.78	,-2.54169E-5}, {-0.77	,-2.53152E-5}, {-0.76	,-2.51382E-5}, {-0.75	
            ,-2.5012E-5	}, {-0.74,-2.49763E-5}, {-0.73	,-2.48979E-5}, {-0.72	,-2.47632E-5}, {-0.71	,-2.44929E-5}, {-0.7	
            ,-2.44019E-5}, {-0.69,-2.42607E-5}, {-0.68	,-2.42726E-5}, {-0.67	,-2.41539E-5}, {-0.66	,-2.42168E-5}, {-0.65	
            ,-2.41945E-5}, {-0.64,-2.40965E-5}, {-0.63	,-2.39313E-5}, {-0.62	,-2.37952E-5}, {-0.61	,-2.37298E-5}, {-0.6	
            ,-2.36305E-5}, {-0.59,-2.35975E-5}, {-0.58	,-2.34759E-5}, {-0.57	,-2.33353E-5}, {-0.56	,-2.32067E-5}, {-0.55	
            ,-2.31281E-5}, {-0.54,-2.29828E-5}, {-0.53	,-2.28304E-5}, {-0.52	,-2.27718E-5}, {-0.51	,-2.2734E-5	}, {-0.5	
            ,-2.26393E-5}, {-0.49,-2.25852E-5}, {-0.48	,-2.26022E-5}, {-0.47	,-2.24753E-5}, {-0.46	,-2.23613E-5}, {-0.45	
            ,-2.22146E-5}, {-0.44,-2.20806E-5}, {-0.43	,-2.19176E-5}, {-0.42	,-2.17885E-5}, {-0.41	,-2.16338E-5}, {-0.4	
            ,-2.15316E-5}, {-0.39,-2.13955E-5}, {-0.38	,-2.14044E-5}, {-0.37	,-2.13087E-5}, {-0.36	,-2.12255E-5}, {-0.35	
            ,-2.11127E-5}, {-0.34,-2.10469E-5}, {-0.33	,-2.09738E-5}, {-0.32	,-2.08772E-5}, {-0.31	,-2.07771E-5}, {-0.3	
            ,-2.06422E-5}, {-0.29,-2.05083E-5}, {-0.28	,-2.03797E-5}, {-0.27	,-2.02258E-5}, {-0.26	,-2.01407E-5}, {-0.25	
            ,-2.00554E-5}, {-0.24,-1.98411E-5}, {-0.23	,-1.96978E-5}, {-0.22	,-1.95826E-5}, {-0.21	,-1.95474E-5}, {-0.2	
            ,-1.95567E-5}, {-0.19,-1.94704E-5}, {-0.18	,-1.93697E-5}, {-0.17	,-1.92351E-5}, {-0.16	,-1.91121E-5}, {-0.15	
            ,-1.88752E-5}, {-0.14,-1.87481E-5}, {-0.13	,-1.87328E-5}, {-0.12	,-1.85662E-5}, {-0.11	,-1.84075E-5}, {-0.1	
            ,-1.82534E-5}, {-0.09,-1.81987E-5}, {-0.08	,-1.81027E-5}, {-0.07	,-1.80562E-5}, {-0.06	,-1.79015E-5}, {-0.05	
            ,-1.78122E-5}, {-0.04,-1.7615E-5	}, {-0.03	,-1.74806E-5}, {-0.02	,-1.72845E-5}, {-0.01	,-1.7134E-5	}, {0.00	
            ,-1.69764E-5}, {0.01,-1.68571E-5}, {0.02	,-1.67272E-5}, {0.03	,-1.66062E-5}, {0.04	,-1.64887E-5}, {0.05	
            ,-1.63169E-5}, {0.06,-1.62079E-5}, {0.07	,-1.60301E-5}, {0.08	,-1.59027E-5}, {0.09	,-1.57271E-5}, {0.10	
            ,-1.55252E-5}, {0.11,-1.5359E-5	}, {0.12	,-1.51605E-5}, {0.13	,-1.50174E-5}, {0.14	,-1.47922E-5}, {0.15	
            ,-1.46296E-5}, {0.16,-1.44069E-5}, {0.17	,-1.42091E-5}, {0.18	,-1.39941E-5}, {0.19	,-1.38147E-5}, {0.20	
            ,-1.36168E-5}, {0.21,-1.34085E-5}, {0.22	,-1.32285E-5}, {0.23	,-1.30056E-5}, {0.24	,-1.27656E-5}, {0.25	
            ,-1.24878E-5}, {0.26,-1.22031E-5}, {0.27	,-1.19399E-5}, {0.28	,-1.16701E-5}, {0.29	,-1.14622E-5}, {0.30	
            ,-1.11313E-5}, {0.31,-1.08531E-5}, {0.32	,-1.05169E-5}, {0.33	,-1.02135E-5}, {0.34	,-9.91881E-6}, {0.35	
            ,-9.61435E-6}, {0.36,-9.28141E-6}, {0.37	,-8.93795E-6}, {0.38	,-8.6225E-6	}, {0.39	,-8.29242E-6}, {0.40	
            ,-7.90502E-6}, {0.41,-7.51848E-6}, {0.42	,-7.12991E-6}, {0.43	,-6.73646E-6}, {0.44	,-6.35606E-6}, {0.45	
            ,-5.94898E-6}, {0.46,-5.54578E-6}, {0.47	,-5.15222E-6}, {0.48	,-4.71162E-6}, {0.49	,-4.26207E-6}, {0.50	
            ,-3.79936E-6}, {0.51,-3.29926E-6}, {0.52	,-2.78974E-6}, {0.53	,-2.25374E-6}, {0.54	,-1.67325E-6}, {0.55	
            ,-1.06235E-6}, {0.56,-4.13555E-7}, {0.57	,2.77079E-7	}, {0.58	,1.03397E-6	}, {0.59	,1.83686E-6	}, {0.60	
            ,2.70241E-6 }, {0.61,3.65319E-6	}, {0.62	,4.67337E-6	}, {0.63	,5.78987E-6	}, {0.64	,6.99645E-6	}, {0.65	
            ,8.31519E-6	}, {0.66,9.74561E-6	}, {0.67	,1.13022E-5	}, {0.68	,1.29961E-5	}, {0.69	,1.48197E-5	}, {0.70	
            ,1.68074E-5 }, {0.71,1.89395E-5	}, {0.72	,2.1246E-5	}, {0.73	,2.37295E-5	}, {0.74	,2.64039E-5	}, {0.75	
            ,2.92761E-5	}, {0.76,3.23403E-5	}, {0.77	,3.56295E-5	}, {0.78	,3.91399E-5	}, {0.79	,4.28893E-5	}, {0.80	
            ,4.68882E-5 }, {0.81,5.11296E-5	}, {0.82	,5.56566E-5	}, {0.83	,6.04712E-5	}, {0.84	,6.5588E-5	}, {0.85	
            ,7.10005E-5	}, {0.86,7.67625E-5	}, {0.87	,8.28716E-5	}, {0.88	,8.93567E-5	}, {0.89	,9.62194E-5	}, {0.90	
            ,1.03495E-4 }, {0.91,1.11199E-4	}, {0.92	,1.19327E-4	}, {0.93	,1.27905E-4	}, {0.94	,1.36979E-4	}, {0.95	
            ,1.46472E-4	}, {0.96,1.56489E-4	}, {0.97	,1.6702E-4	}, {0.98	,1.78044E-4	}, {0.99	,1.89525E-4	}, {1.00	
            ,2.01571E-4 }};//</editor-fold>
        
        
        // Polyfit fails with original GaussNewtonOptimizer implementation;
        // works with LevenbergMarquardtOptimizer
        // <editor-fold defaultstate="collapsed" desc="A dark curve?">
        double[][] PolyFitFails = {{-0.2,-7.12442E-13 }, {-0.199 ,-4.33397E-13 }, {-0.198 ,-2.823E-13 }, {-0.197 ,-1.40405E-13 }, {-0.196 ,-7.80821E-15 }, {-0.195 ,6.20484E-14 }, {-0.194 ,7.24673E-14 }, {-0.193 ,1.47152E-13 }, {-0.192
            ,1.9629E-13 }, {-0.191 ,2.12038E-13 }, {-0.19 ,2.46906E-13 }, {-0.189 ,2.77495E-13 }, {-0.188 ,2.51281E-13 }, {-0.187 ,2.64001E-13 }, {-0.186 ,2.8882E-13 }, {-0.185 ,3.13604E-13 }, {-0.184 ,3.14248E-13 }, {-0.183 ,3.1172E-13 }, {-0.182 ,3.12912E-13
            }, {-0.181 ,3.06761E-13 }, {-0.18 ,2.8559E-13 }, {-0.179 ,2.86806E-13 }, {-0.178 ,2.985E-13 }, {-0.177 ,2.67148E-13 }, {-0.176 ,2.94173E-13 }, {-0.175 ,3.27528E-13 }, {-0.174 ,3.33858E-13 }, {-0.173 ,2.97511E-13 }, {-0.172 ,2.8615E-13 }, {-0.171
            ,2.84624E-13 }, {-0.17 ,2.62034E-13 }, {-0.169 ,2.90036E-13 }, {-0.168 ,2.83527E-13 }, {-0.167 ,2.87271E-13 }, {-0.166 ,2.66147E-13 }, {-0.165 ,3.17526E-13 }, {-0.164 ,3.27849E-13 }, {-0.163 ,3.3685E-13 }, {-0.162 ,3.18944E-13 }, {-0.161 ,2.90084E-13
            }, {-0.16 ,3.3412E-13 }, {-0.159 ,2.74539E-13 }, {-0.158 ,2.62749E-13 }, {-0.157 ,3.03781E-13 }, {-0.156 ,2.95794E-13 }, {-0.155 ,2.85244E-13 }, {-0.154 ,2.21789E-13 }, {-0.153 ,2.49207E-13 }, {-0.152 ,2.87581E-13 }, {-0.151 ,2.79152E-13 }, {-0.15
            ,2.81632E-13 }, {-0.149 ,3.2537E-13 }, {-0.148 ,3.19314E-13 }, {-0.147 ,2.87581E-13 }, {-0.146 ,2.9031E-13 }, {-0.145 ,2.67363E-13 }, {-0.144 ,2.4296E-13 }, {-0.143 ,2.80333E-13 }, {-0.142 ,3.05998E-13 }, {-0.141 ,2.77829E-13 }, {-0.14 ,2.86889E-13
            }, {-0.139 ,2.97427E-13 }, {-0.138 ,2.86555E-13 }, {-0.137 ,2.6443E-13 }, {-0.136 ,2.79725E-13 }, {-0.135 ,2.8156E-13 }, {-0.134 ,3.25334E-13 }, {-0.133 ,3.50606E-13 }, {-0.132 ,3.51083E-13 }, {-0.131 ,3.54075E-13 }, {-0.13 ,2.94459E-13 }, {-0.129
            ,2.71142E-13 }, {-0.128 ,2.59078E-13 }, {-0.127 ,2.44033E-13 }, {-0.126 ,2.87819E-13 }, {-0.125 ,2.85125E-13 }, {-0.124 ,3.11005E-13 }, {-0.123 ,3.09527E-13 }, {-0.122 ,3.25799E-13 }, {-0.121 ,2.97844E-13 }, {-0.12 ,3.10111E-13 }, {-0.119
            ,3.02112E-13 }, {-0.118 ,3.45778E-13 }, {-0.117 ,3.47054E-13 }, {-0.116 ,2.93279E-13 }, {-0.115 ,2.9608E-13 }, {-0.114 ,2.83253E-13 }, {-0.113 ,2.90167E-13 }, {-0.112 ,2.66898E-13 }, {-0.111 ,3.11971E-13 }, {-0.11 ,3.14236E-13 }, {-0.109 ,2.98655E-13
            }, {-0.108 ,2.80917E-13 }, {-0.107 ,2.689E-13 }, {-0.106 ,2.67994E-13 }, {-0.105 ,3.27277E-13 }, {-0.104 ,3.18456E-13 }, {-0.103 ,3.23033E-13 }, {-0.102 ,3.29125E-13 }, {-0.101 ,3.30293E-13 }, {-0.1 ,3.02601E-13 }, {-0.099 ,2.98285E-13 }, {-0.098
            ,2.9434E-13 }, {-0.097 ,2.86269E-13 }, {-0.096 ,3.03614E-13 }, {-0.095 ,2.82455E-13 }, {-0.094 ,3.28052E-13 }, {-0.093 ,3.30865E-13 }, {-0.092 ,3.1656E-13 }, {-0.091 ,3.17597E-13 }, {-0.09 ,3.59821E-13 }, {-0.089 ,3.5826E-13 }, {-0.088 ,3.34013E-13
            }, {-0.087 ,3.17562E-13 }, {-0.086 ,3.02052E-13 }, {-0.085 ,3.03745E-13 }, {-0.084 ,2.83241E-13 }, {-0.083 ,2.78437E-13 }, {-0.082 ,3.17609E-13 }, {-0.081 ,3.13783E-13 }, {-0.08 ,3.35884E-13 }, {-0.079 ,3.29101E-13 }, {-0.078 ,3.43156E-13 }, {-0.077
            ,2.95913E-13 }, {-0.076 ,2.99525E-13 }, {-0.075 ,3.1811E-13 }, {-0.074 ,2.88582E-13 }, {-0.073 ,3.4138E-13 }, {-0.072 ,3.37207E-13 }, {-0.071 ,2.98023E-13 }, {-0.07 ,3.30448E-13 }, {-0.069 ,3.29828E-13 }, {-0.068 ,3.15487E-13 }, {-0.067 ,2.82192E-13
            }, {-0.066 ,3.11875E-13 }, {-0.065 ,3.00717E-13 }, {-0.064 ,3.36254E-13 }, {-0.063 ,3.40629E-13 }, {-0.062 ,3.1718E-13 }, {-0.061 ,3.17085E-13 }, {-0.06 ,3.15571E-13 }, {-0.059 ,3.23009E-13 }, {-0.058 ,3.3288E-13 }, {-0.057 ,3.18515E-13 }, {-0.056
            ,3.21746E-13 }, {-0.055 ,2.93624E-13 }, {-0.054 ,2.99799E-13 }, {-0.053 ,3.14236E-13 }, {-0.052 ,2.88939E-13 }, {-0.051 ,3.16012E-13 }, {-0.05 ,3.14629E-13 }, {-0.049 ,3.24833E-13 }, {-0.048 ,3.35777E-13 }, {-0.047 ,3.1054E-13 }, {-0.046 ,3.10516E-13
            }, {-0.045 ,3.19242E-13 }, {-0.044 ,2.94256E-13 }, {-0.043 ,3.12507E-13 }, {-0.042 ,3.44646E-13 }, {-0.041 ,3.22652E-13 }, {-0.04 ,3.14069E-13 }, {-0.039 ,3.34871E-13 }, {-0.038 ,3.42608E-13 }, {-0.037 ,3.07095E-13 }, {-0.036 ,3.05462E-13 }, {-0.035
            ,3.25024E-13 }, {-0.034 ,3.08609E-13 }, {-0.033 ,3.28183E-13 }, {-0.032 ,3.1538E-13 }, {-0.031 ,2.91848E-13 }, {-0.03 ,3.04127E-13 }, {-0.029 ,3.0961E-13 }, {-0.028 ,2.97689E-13 }, {-0.027 ,2.94924E-13 }, {-0.026 ,2.98965E-13 }, {-0.025 ,3.40736E-13
            }, {-0.024 ,3.0719E-13 }, {-0.023 ,3.02911E-13 }, {-0.022 ,3.19147E-13 }, {-0.021 ,3.25632E-13 }, {-0.02 ,3.12614E-13 }, {-0.019 ,3.2562E-13 }, {-0.018 ,3.15571E-13 }, {-0.017 ,3.04425E-13 }, {-0.016 ,3.14903E-13 }, {-0.015 ,3.0055E-13 }, {-0.014
            ,2.65968E-13 }, {-0.013 ,2.5847E-13 }, {-0.012 ,2.71654E-13 }, {-0.011 ,3.14927E-13 }, {-0.01 ,3.06892E-13 }, {-0.0090 ,3.03245E-13 }, {-0.0080 ,2.8441E-13 }, {-0.0070 ,3.18325E-13 }, {-0.0060 ,3.19171E-13 }, {-0.0050 ,2.85125E-13 }, {-0.0040
            ,2.9608E-13 }, {-0.0030 ,2.91729E-13 }, {-0.0020 ,3.08478E-13 }, {-0.0010 ,2.8156E-13 }, {0.0 ,-6.47306E-15 }, {0.0010 ,7.75814E-14 }, {0.0020 ,1.30534E-13 }, {0.0030 ,1.8785E-13 }, {0.0040 ,1.93548E-13 }, {0.0050 ,2.60568E-13 }, {0.0060 ,2.67148E-13
            }, {0.0070 ,2.63786E-13 }, {0.0080 ,2.66361E-13 }, {0.0090 ,2.8286E-13 }, {0.01 ,3.03769E-13 }, {0.011 ,2.5543E-13 }, {0.012 ,2.83074E-13 }, {0.013 ,2.94244E-13 }, {0.014 ,2.96926E-13 }, {0.015 ,3.10576E-13 }, {0.016 ,3.06559E-13 }, {0.017
            ,3.59011E-13 }, {0.018 ,3.26312E-13 }, {0.019 ,3.47137E-13 }, {0.02 ,3.22425E-13 }, {0.021 ,3.17228E-13 }, {0.022 ,3.24118E-13 }, {0.023 ,2.73407E-13 }, {0.024 ,3.10838E-13 }, {0.025 ,2.92468E-13 }, {0.026 ,3.29947E-13 }, {0.027 ,3.35073E-13 },
            {0.028 ,2.81632E-13 }, {0.029 ,2.99835E-13 }, {0.03 ,3.12304E-13 }, {0.031 ,3.03197E-13 }, {0.032 ,2.75266E-13 }, {0.033 ,3.00801E-13 }, {0.034 ,3.11375E-13 }, {0.035 ,3.21209E-13 }, {0.036 ,2.97701E-13 }, {0.037 ,2.92516E-13 }, {0.038 ,3.09324E-13
            }, {0.039 ,3.18813E-13 }, {0.04 ,2.97642E-13 }, {0.041 ,3.02637E-13 }, {0.042 ,3.16119E-13 }, {0.043 ,3.30865E-13 }, {0.044 ,3.36385E-13 }, {0.045 ,3.07989E-13 }, {0.046 ,3.2568E-13 }, {0.047 ,3.17645E-13 }, {0.048 ,2.99883E-13 }, {0.049 ,2.92146E-13
            }, {0.05 ,3.11732E-13 }, {0.051 ,3.1606E-13 }, {0.052 ,3.28517E-13 }, {0.053 ,3.07989E-13 }, {0.054 ,3.13687E-13 }, {0.055 ,3.16763E-13 }, {0.056 ,3.2016E-13 }, {0.057 ,3.32642E-13 }, {0.058 ,3.20506E-13 }, {0.059 ,3.10636E-13 }, {0.06 ,3.27981E-13
            }, {0.061 ,3.2357E-13 }, {0.062 ,3.09086E-13 }, {0.063 ,2.92277E-13 }, {0.064 ,3.23427E-13 }, {0.065 ,3.36158E-13 }, {0.066 ,3.27194E-13 }, {0.067 ,3.15404E-13 }, {0.068 ,3.41475E-13 }, {0.069 ,3.23904E-13 }, {0.07 ,3.02887E-13 }, {0.071 ,3.07643E-13
            }, {0.072 ,3.3865E-13 }, {0.073 ,3.36432E-13 }, {0.074 ,3.16036E-13 }, {0.075 ,3.2382E-13 }, {0.076 ,3.2016E-13 }, {0.077 ,3.18146E-13 }, {0.078 ,3.06547E-13 }, {0.079 ,2.90704E-13 }, {0.08 ,3.14236E-13 }, {0.081 ,2.55084E-13 }, {0.082 ,2.97904E-13
            }, {0.083 ,3.08406E-13 }, {0.084 ,3.15797E-13 }, {0.085 ,3.14534E-13 }, {0.086 ,3.19326E-13 }, {0.087 ,3.19326E-13 }, {0.088 ,3.15964E-13 }, {0.089 ,2.80225E-13 }, {0.09 ,2.97809E-13 }, {0.091 ,3.02303E-13 }, {0.092 ,3.21722E-13 }, {0.093
            ,3.15595E-13 }, {0.094 ,2.93899E-13 }, {0.095 ,3.02911E-13 }, {0.096 ,3.10254E-13 }, {0.097 ,3.28803E-13 }, {0.098 ,3.04067E-13 }, {0.099 ,2.7101E-13 }, {0.1 ,1.96743E-13 }, {0.101 ,1.93393E-13 }, {0.102 ,2.38168E-13 }, {0.103 ,2.38514E-13 }, {0.104
            ,2.8795E-13 }, {0.105 ,3.10159E-13 }, {0.106 ,2.94924E-13 }, {0.107 ,3.04675E-13 }, {0.108 ,2.98309E-13 }, {0.109 ,3.1811E-13 }, {0.11 ,3.36409E-13 }, {0.111 ,3.3251E-13 }, {0.112 ,3.38995E-13 }, {0.113 ,3.00086E-13 }, {0.114 ,2.85578E-13 }, {0.115
            ,2.96211E-13 }, {0.116 ,2.89083E-13 }, {0.117 ,2.78747E-13 }, {0.118 ,3.20637E-13 }, {0.119 ,3.24523E-13 }, {0.12 ,3.1215E-13 }, {0.121 ,3.12841E-13 }, {0.122 ,3.32808E-13 }, {0.123 ,3.15201E-13 }, {0.124 ,2.94995E-13 }, {0.125 ,3.39365E-13 }, {0.126
            ,3.5069E-13 }, {0.127 ,3.52538E-13 }, {0.128 ,3.36456E-13 }, {0.129 ,3.36671E-13 }, {0.13 ,3.26335E-13 }, {0.131 ,3.30293E-13 }, {0.132 ,3.3536E-13 }, {0.133 ,3.15511E-13 }, {0.134 ,3.03519E-13 }, {0.135 ,3.25251E-13 }, {0.136 ,3.18658E-13 }, {0.137
            ,3.00002E-13 }, {0.138 ,3.11875E-13 }, {0.139 ,3.29745E-13 }, {0.14 ,3.59917E-13 }, {0.141 ,3.12841E-13 }, {0.142 ,2.80333E-13 }, {0.143 ,3.01254E-13 }, {0.144 ,3.23451E-13 }, {0.145 ,3.31223E-13 }, {0.146 ,3.12173E-13 }, {0.147 ,3.48723E-13 },
            {0.148 ,3.33762E-13 }, {0.149 ,3.18527E-13 }, {0.15 ,2.98619E-13 }, {0.151 ,3.20172E-13 }, {0.152 ,2.84779E-13 }, {0.153 ,2.6505E-13 }, {0.154 ,2.76256E-13 }, {0.155 ,3.2109E-13 }, {0.156 ,3.1395E-13 }, {0.157 ,3.00622E-13 }, {0.158 ,3.02362E-13 },
            {0.159 ,3.49414E-13 }, {0.16 ,3.32332E-13 }, {0.161 ,3.00241E-13 }, {0.162 ,2.93982E-13 }, {0.163 ,3.08275E-13 }, {0.164 ,3.07691E-13 }, {0.165 ,2.95663E-13 }, {0.166 ,3.0669E-13 }, {0.167 ,2.89869E-13 }, {0.168 ,3.26002E-13 }, {0.169 ,3.09944E-13 },
            {0.17 ,3.02637E-13 }, {0.171 ,3.02052E-13 }, {0.172 ,3.43013E-13 }, {0.173 ,3.12638E-13 }, {0.174 ,3.07488E-13 }, {0.175 ,2.92408E-13 }, {0.176 ,2.9552E-13 }, {0.177 ,2.68829E-13 }, {0.178 ,3.18158E-13 }, {0.179 ,3.29399E-13 }, {0.18 ,3.4138E-13 },
            {0.181 ,3.52216E-13 }, {0.182 ,3.26371E-13 }, {0.183 ,3.0781E-13 }, {0.184 ,3.01886E-13 }, {0.185 ,3.21424E-13 }, {0.186 ,3.07858E-13 }, {0.187 ,3.02899E-13 }, {0.188 ,3.32081E-13 }, {0.189 ,3.48067E-13 }, {0.19 ,3.07095E-13 }, {0.191 ,2.49803E-13 },
            {0.192 ,3.23474E-13 }, {0.193 ,2.81966E-13 }, {0.194 ,2.70212E-13 }, {0.195 ,2.99656E-13 }, {0.196 ,3.28481E-13 }, {0.197 ,3.30043E-13 }, {0.198 ,2.21944E-13 }, {0.199 ,2.67565E-13 }, {0.2 ,2.73418E-13 }, {0.201 ,2.87509E-13 }, {0.202 ,2.64657E-13 },
            {0.203 ,2.42198E-13 }, {0.204 ,2.88701E-13 }, {0.205 ,3.08526E-13 }, {0.206 ,2.80619E-13 }, {0.207 ,3.02637E-13 }, {0.208 ,2.97403E-13 }, {0.209 ,3.53277E-13 }, {0.21 ,3.67284E-13 }, {0.211 ,2.81858E-13 }, {0.212 ,2.96891E-13 }, {0.213 ,3.51346E-13
            }, {0.214 ,3.26836E-13 }, {0.215 ,3.2289E-13 }, {0.216 ,3.07691E-13 }, {0.217 ,3.1786E-13 }, {0.218 ,3.34859E-13 }, {0.219 ,2.99633E-13 }, {0.22 ,2.90382E-13 }, {0.221 ,2.7231E-13 }, {0.222 ,3.42202E-13 }, {0.223 ,2.97678E-13 }, {0.224 ,3.11506E-13
            }, {0.225 ,3.13771E-13 }, {0.226 ,2.99489E-13 }, {0.227 ,3.09002E-13 }, {0.228 ,3.42941E-13 }, {0.229 ,3.09455E-13 }, {0.23 ,3.13747E-13 }, {0.231 ,3.19195E-13 }, {0.232 ,2.59626E-13 }, {0.233 ,2.74384E-13 }, {0.234 ,2.79582E-13 }, {0.235 ,2.9844E-13
            }, {0.236 ,2.86973E-13 }, {0.237 ,3.36373E-13 }, {0.238 ,3.17883E-13 }, {0.239 ,3.02601E-13 }, {0.24 ,3.22127E-13 }, {0.241 ,3.36039E-13 }, {0.242 ,2.93326E-13 }, {0.243 ,2.90239E-13 }, {0.244 ,2.55966E-13 }, {0.245 ,2.88618E-13 }, {0.246
            ,2.74885E-13 }, {0.247 ,2.79903E-13 }, {0.248 ,2.4085E-13 }, {0.249 ,2.47622E-13 }, {0.25 ,2.76899E-13 }, {0.251 ,3.08406E-13 }, {0.252 ,2.79236E-13 }, {0.253 ,2.7535E-13 }, {0.254 ,3.05176E-13 }, {0.255 ,3.27861E-13 }, {0.256 ,3.00252E-13 }, {0.257
            ,1.14667E-13 }, {0.258 ,1.59013E-13 }, {0.259 ,1.82486E-13 }, {0.26 ,2.39205E-13 }, {0.261 ,2.4358E-13 }, {0.262 ,2.3067E-13 }, {0.263 ,2.90406E-13 }, {0.264 ,2.92969E-13 }, {0.265 ,2.89607E-13 }, {0.266 ,2.71022E-13 }, {0.267 ,2.91598E-13 }, {0.268
            ,2.97952E-13 }, {0.269 ,2.95925E-13 }, {0.27 ,3.19946E-13 }, {0.271 ,3.05164E-13 }, {0.272 ,3.16334E-13 }, {0.273 ,3.01623E-13 }, {0.274 ,3.31509E-13 }, {0.275 ,3.31366E-13 }, {0.276 ,3.12889E-13 }, {0.277 ,2.9856E-13 }, {0.278 ,3.20995E-13 }, {0.279
            ,3.30079E-13 }, {0.28 ,3.03638E-13 }, {0.281 ,3.08895E-13 }, {0.282 ,3.13437E-13 }, {0.283 ,3.0483E-13 }, {0.284 ,2.72048E-13 }, {0.285 ,3.22974E-13 }, {0.286 ,3.71039E-13 }, {0.287 ,3.12972E-13 }, {0.288 ,2.87116E-13 }, {0.289 ,3.17967E-13 }, {0.29
            ,3.08764E-13 }, {0.291 ,3.25787E-13 }, {0.292 ,2.91145E-13 }, {0.293 ,3.13056E-13 }, {0.294 ,3.38423E-13 }, {0.295 ,3.27086E-13 }, {0.296 ,3.25084E-13 }, {0.297 ,3.21257E-13 }, {0.298 ,3.26335E-13 }, {0.299 ,2.96819E-13 }, {0.3 ,1.46973E-13 }, {0.301
            ,1.78969E-13 }, {0.302 ,2.30527E-13 }, {0.303 ,2.64454E-13 }, {0.304 ,2.69425E-13 }, {0.305 ,3.06249E-13 }, {0.306 ,3.11434E-13 }, {0.307 ,3.44741E-13 }, {0.308 ,3.13938E-13 }, {0.309 ,3.1482E-13 }, {0.31 ,2.96581E-13 }, {0.311 ,2.93696E-13 }, {0.312
            ,2.6139E-13 }, {0.313 ,3.01504E-13 }, {0.314 ,3.01647E-13 }, {0.315 ,3.38113E-13 }, {0.316 ,3.08597E-13 }, {0.317 ,3.33953E-13 }, {0.318 ,3.17395E-13 }, {0.319 ,3.13663E-13 }, {0.32 ,2.85387E-13 }, {0.321 ,2.80166E-13 }, {0.322 ,3.37565E-13 }, {0.323
            ,3.25882E-13 }, {0.324 ,2.64597E-13 }, {0.325 ,2.92182E-13 }, {0.326 ,2.97976E-13 }, {0.327 ,3.12388E-13 }, {0.328 ,2.97093E-13 }, {0.329 ,3.2326E-13 }, {0.33 ,3.51822E-13 }, {0.331 ,3.36218E-13 }, {0.332 ,2.8131E-13 }, {0.333 ,2.79236E-13 }, {0.334
            ,3.18933E-13 }, {0.335 ,3.11172E-13 }, {0.336 ,3.03471E-13 }, {0.337 ,2.92099E-13 }, {0.338 ,3.14903E-13 }, {0.339 ,3.23772E-13 }, {0.34 ,3.14879E-13 }, {0.341 ,3.29828E-13 }, {0.342 ,3.13413E-13 }, {0.343 ,3.01647E-13 }, {0.344 ,2.84421E-13 },
            {0.345 ,2.92981E-13 }, {0.346 ,3.11935E-13 }, {0.347 ,2.88534E-13 }, {0.348 ,2.96509E-13 }, {0.349 ,3.24225E-13 }, {0.35 ,3.55351E-13 }, {0.351 ,3.21794E-13 }, {0.352 ,3.4548E-13 }, {0.353 ,3.07441E-13 }, {0.354 ,3.63505E-13 }, {0.355 ,2.98893E-13 },
            {0.356 ,3.06678E-13 }, {0.357 ,2.90787E-13 }, {0.358 ,2.82156E-13 }, {0.359 ,2.92647E-13 }, {0.36 ,3.45802E-13 }, {0.361 ,3.03984E-13 }, {0.362 ,3.00741E-13 }, {0.363 ,2.95579E-13 }, {0.364 ,3.21579E-13 }, {0.365 ,3.13663E-13 }, {0.366 ,3.10111E-13
            }, {0.367 ,3.04461E-13 }, {0.368 ,3.47853E-13 }, {0.369 ,3.0365E-13 }, {0.37 ,3.02863E-13 }, {0.371 ,3.15869E-13 }, {0.372 ,3.91364E-13 }, {0.373 ,3.16918E-13 }, {0.374 ,2.88582E-13 }, {0.375 ,3.37815E-13 }, {0.376 ,3.49569E-13 }, {0.377 ,3.64244E-13
            }, {0.378 ,3.27337E-13 }, {0.379 ,3.39198E-13 }, {0.38 ,3.14152E-13 }, {0.381 ,2.62666E-13 }, {0.382 ,2.48289E-13 }, {0.383 ,2.56944E-13 }, {0.384 ,3.21841E-13 }, {0.385 ,3.10874E-13 }, {0.386 ,3.2618E-13 }, {0.387 ,3.09002E-13 }, {0.388 ,3.67033E-13
            }, {0.389 ,3.49808E-13 }, {0.39 ,3.20673E-13 }, {0.391 ,2.95842E-13 }, {0.392 ,3.23737E-13 }, {0.393 ,3.25382E-13 }, {0.394 ,3.47817E-13 }, {0.395 ,3.07608E-13 }, {0.396 ,3.33738E-13 }, {0.397 ,3.56233E-13 }, {0.398 ,3.19505E-13 }, {0.399
            ,3.02017E-13 }, {0.4 ,2.65622E-13 }, {0.401 ,2.91228E-13 }, {0.402 ,3.07846E-13 }, {0.403 ,2.96688E-13 }, {0.404 ,2.68185E-13 }, {0.405 ,3.52621E-13 }, {0.406 ,3.54433E-13 }, {0.407 ,3.46041E-13 }, {0.408 ,3.06392E-13 }, {0.409 ,3.08061E-13 }, {0.41
            ,3.27361E-13 }, {0.411 ,3.03948E-13 }, {0.412 ,2.88451E-13 }, {0.413 ,2.89154E-13 }, {0.414 ,3.40819E-13 }, {0.415 ,3.10838E-13 }, {0.416 ,3.22759E-13 }, {0.417 ,3.32248E-13 }, {0.418 ,3.46422E-13 }, {0.419 ,3.1656E-13 }, {0.42 ,2.99418E-13 }, {0.421
            ,3.05212E-13 }, {0.422 ,3.034E-13 }, {0.423 ,3.08597E-13 }, {0.424 ,2.88641E-13 }, {0.425 ,3.08442E-13 }, {0.426 ,3.26729E-13 }, {0.427 ,2.5506E-13 }, {0.428 ,2.97868E-13 }, {0.429 ,3.35836E-13 }, {0.43 ,3.09885E-13 }, {0.431 ,2.99799E-13 }, {0.432
            ,3.13389E-13 }, {0.433 ,3.29566E-13 }, {0.434 ,2.9912E-13 }, {0.435 ,3.00968E-13 }, {0.436 ,2.93064E-13 }, {0.437 ,3.43478E-13 }, {0.438 ,3.22902E-13 }, {0.439 ,2.97952E-13 }, {0.44 ,3.1029E-13 }, {0.441 ,2.99525E-13 }, {0.442 ,2.94375E-13 }, {0.443
            ,2.65586E-13 }, {0.444 ,2.89154E-13 }, {0.445 ,3.09992E-13 }, {0.446 ,2.96724E-13 }, {0.447 ,3.2407E-13 }, {0.448 ,3.54302E-13 }, {0.449 ,3.6546E-13 }, {0.45 ,3.13115E-13 }, {0.451 ,3.33238E-13 }, {0.452 ,2.97439E-13 }, {0.453 ,3.17299E-13 }, {0.454
            ,3.01969E-13 }, {0.455 ,3.15833E-13 }, {0.456 ,3.10194E-13 }, {0.457 ,3.29328E-13 }, {0.458 ,3.26967E-13 }, {0.459 ,3.27814E-13 }, {0.46 ,3.33178E-13 }, {0.461 ,3.31283E-13 }, {0.462 ,2.97689E-13 }, {0.463 ,3.31116E-13 }, {0.464 ,3.25167E-13 },
            {0.465 ,3.37791E-13 }, {0.466 ,2.7597E-13 }, {0.467 ,2.59554E-13 }, {0.468 ,2.82061E-13 }, {0.469 ,2.76649E-13 }, {0.47 ,3.09527E-13 }, {0.471 ,2.89154E-13 }, {0.472 ,3.24667E-13 }, {0.473 ,3.24321E-13 }, {0.474 ,2.97952E-13 }, {0.475 ,2.99668E-13 },
            {0.476 ,3.06416E-13 }, {0.477 ,2.78652E-13 }, {0.478 ,3.02184E-13 }, {0.479 ,2.82407E-13 }, {0.48 ,2.91562E-13 }, {0.481 ,3.00503E-13 }, {0.482 ,3.03733E-13 }, {0.483 ,3.11792E-13 }, {0.484 ,3.02994E-13 }, {0.485 ,3.34704E-13 }, {0.486 ,2.96128E-13
            }, {0.487 ,3.2239E-13 }, {0.488 ,3.14522E-13 }, {0.489 ,3.20804E-13 }, {0.49 ,3.38519E-13 }, {0.491 ,3.01337E-13 }, {0.492 ,3.22556E-13 }, {0.493 ,3.15034E-13 }, {0.494 ,3.20208E-13 }, {0.495 ,3.1234E-13 }, {0.496 ,2.91467E-13 }, {0.497 ,3.10743E-13
            }, {0.498 ,2.90418E-13 }, {0.499 ,2.98107E-13 }, {0.5 ,2.83182E-13 }, {0.501 ,3.05712E-13 }, {0.502 ,2.8466E-13 }, {0.503 ,3.21496E-13 }, {0.504 ,3.27682E-13 }, {0.505 ,3.51381E-13 }, {0.506 ,3.28481E-13 }, {0.507 ,3.38984E-13 }, {0.508 ,3.00288E-13
            }, {0.509 ,3.00753E-13 }, {0.51 ,2.80547E-13 }, {0.511 ,2.92611E-13 }, {0.512 ,2.97868E-13 }, {0.513 ,3.02637E-13 }, {0.514 ,3.33703E-13 }, {0.515 ,2.84708E-13 }, {0.516 ,3.31044E-13 }, {0.517 ,3.07214E-13 }, {0.518 ,3.08573E-13 }, {0.519
            ,2.81084E-13 }, {0.52 ,1.99807E-13 }, {0.521 ,2.19297E-13 }, {0.522 ,2.38049E-13 }, {0.523 ,2.39336E-13 }, {0.524 ,3.12507E-13 }, {0.525 ,3.00539E-13 }, {0.526 ,2.67828E-13 }, {0.527 ,2.58291E-13 }, {0.528 ,2.78246E-13 }, {0.529 ,2.81131E-13 }, {0.53
            ,2.55346E-13 }, {0.531 ,2.93779E-13 }, {0.532 ,3.09491E-13 }, {0.533 ,3.60239E-13 }, {0.534 ,3.11291E-13 }, {0.535 ,3.30281E-13 }, {0.536 ,3.30889E-13 }, {0.537 ,3.28934E-13 }, {0.538 ,3.02911E-13 }, {0.539 ,3.17967E-13 }, {0.54 ,3.05784E-13 },
            {0.541 ,3.13938E-13 }, {0.542 ,3.29351E-13 }, {0.543 ,2.83349E-13 }, {0.544 ,2.85292E-13 }, {0.545 ,3.05092E-13 }, {0.546 ,1.91152E-13 }, {0.547 ,2.5059E-13 }, {0.548 ,2.80714E-13 }, {0.549 ,3.07691E-13 }, {0.55 ,3.16381E-13 }, {0.551 ,2.9093E-13 },
            {0.552 ,3.22485E-13 }, {0.553 ,3.24738E-13 }, {0.554 ,2.72322E-13 }, {0.555 ,2.87187E-13 }, {0.556 ,3.09241E-13 }, {0.557 ,2.70033E-13 }, {0.558 ,2.64966E-13 }, {0.559 ,2.66051E-13 }, {0.56 ,2.99668E-13 }, {0.561 ,3.07691E-13 }, {0.562 ,3.08907E-13
            }, {0.563 ,3.01647E-13 }, {0.564 ,3.4889E-13 }, {0.565 ,2.97904E-13 }, {0.566 ,2.80035E-13 }, {0.567 ,2.70331E-13 }, {0.568 ,2.59125E-13 }, {0.569 ,2.64609E-13 }, {0.57 ,2.82776E-13 }, {0.571 ,2.75421E-13 }, {0.572 ,3.25751E-13 }, {0.573 ,2.9273E-13
            }, {0.574 ,3.407E-13 }, {0.575 ,3.13568E-13 }, {0.576 ,3.58546E-13 }, {0.577 ,2.85625E-13 }, {0.578 ,3.29816E-13 }, {0.579 ,3.07274E-13 }, {0.58 ,3.43132E-13 }, {0.581 ,3.11756E-13 }, {0.582 ,3.1234E-13 }, {0.583 ,3.32439E-13 }, {0.584 ,3.07226E-13
            }, {0.585 ,2.78616E-13 }, {0.586 ,3.32975E-13 }, {0.587 ,3.11685E-13 }, {0.588 ,3.04353E-13 }, {0.589 ,3.31783E-13 }, {0.59 ,3.03531E-13 }, {0.591 ,3.04055E-13 }, {0.592 ,2.67816E-13 }, {0.593 ,3.11351E-13 }, {0.594 ,2.90024E-13 }, {0.595
            ,3.14486E-13 }, {0.596 ,3.06094E-13 }, {0.597 ,2.89857E-13 }, {0.598 ,3.30329E-13 }, {0.599 ,3.07024E-13 }, {0.6 ,3.20923E-13 }, {0.601 ,3.35407E-13 }, {0.602 ,3.02839E-13 }, {0.603 ,3.09277E-13 }, {0.604 ,2.91085E-13 }, {0.605 ,3.16215E-13 }, {0.606
            ,2.91562E-13 }, {0.607 ,3.20697E-13 }, {0.608 ,3.057E-13 }, {0.609 ,3.27134E-13 }, {0.61 ,3.31116E-13 }, {0.611 ,2.8795E-13 }, {0.612 ,3.02398E-13 }, {0.613 ,2.98321E-13 }, {0.614 ,3.40843E-13 }, {0.615 ,3.03638E-13 }, {0.616 ,3.31891E-13 }, {0.617
            ,3.19111E-13 }, {0.618 ,3.39448E-13 }, {0.619 ,3.2928E-13 }, {0.62 ,3.41451E-13 }, {0.621 ,3.28696E-13 }, {0.622 ,3.33655E-13 }, {0.623 ,3.29137E-13 }, {0.624 ,2.70212E-13 }, {0.625 ,3.27349E-13 }, {0.626 ,3.39854E-13 }, {0.627 ,3.16751E-13 }, {0.628
            ,3.06463E-13 }, {0.629 ,3.05629E-13 }, {0.63 ,3.44312E-13 }, {0.631 ,3.54731E-13 }, {0.632 ,3.31867E-13 }, {0.633 ,2.98071E-13 }, {0.634 ,3.25525E-13 }, {0.635 ,3.22592E-13 }, {0.636 ,3.06344E-13 }, {0.637 ,2.62928E-13 }, {0.638 ,3.20113E-13 },
            {0.639 ,2.98393E-13 }, {0.64 ,3.38972E-13 }, {0.641 ,3.23486E-13 }, {0.642 ,3.34036E-13 }, {0.643 ,3.03268E-13 }, {0.644 ,2.8826E-13 }, {0.645 ,3.13568E-13 }, {0.646 ,2.79653E-13 }, {0.647 ,3.06213E-13 }, {0.648 ,3.30842E-13 }, {0.649 ,3.7024E-13 },
            {0.65 ,3.33261E-13 }, {0.651 ,3.31461E-13 }, {0.652 ,3.46458E-13 }, {0.653 ,3.49748E-13 }, {0.654 ,3.17848E-13 }, {0.655 ,3.17562E-13 }, {0.656 ,3.02505E-13 }, {0.657 ,2.91812E-13 }, {0.658 ,3.08573E-13 }, {0.659 ,2.86424E-13 }, {0.66 ,3.01301E-13 },
            {0.661 ,3.38757E-13 }, {0.662 ,3.58725E-13 }, {0.663 ,3.17097E-13 }, {0.664 ,3.21043E-13 }, {0.665 ,3.04568E-13 }, {0.666 ,2.90084E-13 }, {0.667 ,2.87378E-13 }, {0.668 ,3.03578E-13 }, {0.669 ,3.38304E-13 }, {0.67 ,3.08931E-13 }, {0.671 ,3.78358E-13
            }, {0.672 ,3.37672E-13 }, {0.673 ,3.24249E-13 }, {0.674 ,2.83182E-13 }, {0.675 ,2.76566E-13 }, {0.676 ,3.17037E-13 }, {0.677 ,2.88749E-13 }, {0.678 ,3.14605E-13 }, {0.679 ,2.76768E-13 }, {0.68 ,3.21054E-13 }, {0.681 ,3.17848E-13 }, {0.682
            ,2.93696E-13 }, {0.683 ,3.6267E-13 }, {0.684 ,3.09277E-13 }, {0.685 ,2.81525E-13 }, {0.686 ,3.00002E-13 }, {0.687 ,2.94328E-13 }, {0.688 ,2.7734E-13 }, {0.689 ,2.79367E-13 }, {0.69 ,3.11661E-13 }, {0.691 ,2.69794E-13 }, {0.692 ,3.24285E-13 }, {0.693
            ,3.0576E-13 }, {0.694 ,3.51191E-13 }, {0.695 ,2.90453E-13 }, {0.696 ,3.00372E-13 }, {0.697 ,3.32057E-13 }, {0.698 ,3.44682E-13 }, {0.699 ,3.25167E-13 }, {0.7 ,3.18158E-13 }};//</editor-fold>
        
        
        // select data sets to test
        List<double[][]> DataList = Arrays.asList(James, Wei, PolyFitFails);
        
        
        // instantiate Keithley2600 class
        Keithley2600 dev = new Keithley2600();
        
        // iterate through all data sets        
        for(int t=1; t <= DataList.size(); t++) {
        
            // get current data set
            double[][] data = DataList.get(t-1);

            // make a new XYChart to view I(V)
            dev.m_IV_Chart = new iC_ChartXY("unit test " + t, 
                                    "Voltage [V]", "Current [A]",
                                    false  /*legend*/,
                                    640, 480);

            // style for MeasureOV
            SeriesIdentification IV_Series = dev.m_IV_Chart.AddXYSeries("IV",
                    0, false, true, dev.m_IV_Chart.LINE_NONE, dev.m_IV_Chart.MARKER_CIRCLE);

            // set number formats
            dev.m_IV_Chart.getYAxis(0).setNumberFormatOverride(new DecimalFormat("##0.#####E0"));

            // show zero lines
            dev.m_IV_Chart.ShowZeroLines(true, true);

            // add the data
            for (int i=0; i<data.length; i++) {
                dev.m_IV_Chart.AddXYDataPoint(IV_Series, data[i][0], data[i][1]);
            }



            // EvaluateOPV was private, but was made public to be able to test it
            // could have also used PriviledgedAccess and Reflection to access private
            // member as explained here: http://onjava.com/pub/a/onjava/2003/11/12/reflection.html?page=2
            assertTrue("Evaluating data set " + t + " failed",
                    dev.EvaluateOPV(data, "_EvaluateOPV_testcase" + t + ".txt"));
            
            
            // build the file name
            String FileName = m_GUI.getFileName("_EvaluateOPV_testcase" + t + ".png");
            
            // save the graph as png
            dev.m_IV_Chart.SaveAsPNG(new File(FileName), 0, 0);
        }
        
        // TODO 3* try making a non-modal dialog and display it in tearDown. put
        // the non-modal dialog in IcontrolTests
        // give the tester a chance to evaluate the fit quality
//        JOptionPane.showMessageDialog(null, 
//                "Inspect the fitting result and judge if the fit is okay.");
        
        
        // message to the tester
        IcontrolAppMockup.getView().DisplayStatusMessage(
                "--->EvaluateOPV test: inspect the text and graphic files generated in iC/UnitTests/ "
                + "to judge the quality of the fits and evaluations.");
        
        
        
    }
}
