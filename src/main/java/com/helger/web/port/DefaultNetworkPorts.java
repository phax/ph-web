/**
 * Copyright (C) 2006-2014 phloc systems (www.phloc.com)
 * Copyright (C) 2014 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.web.port;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.helger.commons.annotations.CodingStyleguideUnaware;
import com.helger.commons.annotations.PresentForCodeCoverage;
import com.helger.commons.annotations.ReturnsMutableCopy;
import com.helger.commons.collections.ContainerHelper;

/**
 * A list of default ports between 0 and 1024. Created from the IANA port list.
 * http://www.iana.org/assignments/port-numbers
 * 
 * @author Philip Helger
 */
@CodingStyleguideUnaware ("Concerning the constants used in this class")
public final class DefaultNetworkPorts
{
  // needs to be the first member!
  private static List <NetworkPort> s_aPortList = new ArrayList <NetworkPort> ();

  public static final INetworkPort TCP_0_itunes = _registerPort (0,
                                                                 ENetworkProtocol.TCP,
                                                                 "spr-itunes/spl-itunes",
                                                                 "Shirt Pocket netTunes/launchTunes");
  public static final INetworkPort UDP_1_tcpmux = _registerPort (1,
                                                                 ENetworkProtocol.UDP,
                                                                 "tcpmux",
                                                                 "TCP Port Service Multiplexer");
  public static final INetworkPort TCP_1_tcpmux = _registerPort (1,
                                                                 ENetworkProtocol.TCP,
                                                                 "tcpmux",
                                                                 "TCP Port Service Multiplexer");
  public static final INetworkPort TCP_2_compressnet = _registerPort (2,
                                                                      ENetworkProtocol.TCP,
                                                                      "compressnet",
                                                                      "Management Utility");
  public static final INetworkPort UDP_2_compressnet = _registerPort (2,
                                                                      ENetworkProtocol.UDP,
                                                                      "compressnet",
                                                                      "Management Utility");
  public static final INetworkPort TCP_3_compressnet = _registerPort (3,
                                                                      ENetworkProtocol.TCP,
                                                                      "compressnet",
                                                                      "Compression Process");
  public static final INetworkPort UDP_3_compressnet = _registerPort (3,
                                                                      ENetworkProtocol.UDP,
                                                                      "compressnet",
                                                                      "Compression Process");
  public static final INetworkPort UDP_5_rje = _registerPort (5, ENetworkProtocol.UDP, "rje", "Remote Job Entry");
  public static final INetworkPort TCP_5_rje = _registerPort (5, ENetworkProtocol.TCP, "rje", "Remote Job Entry");
  public static final INetworkPort UDP_7_echo = _registerPort (7, ENetworkProtocol.UDP, "echo", "Echo");
  public static final INetworkPort TCP_7_echo = _registerPort (7, ENetworkProtocol.TCP, "echo", "Echo");
  public static final INetworkPort UDP_9_discard = _registerPort (9, ENetworkProtocol.UDP, "discard", "Discard");
  public static final INetworkPort TCP_9_discard = _registerPort (9, ENetworkProtocol.TCP, "discard", "Discard");
  public static final INetworkPort UDP_11_systat = _registerPort (11, ENetworkProtocol.UDP, "systat", "Active Users");
  public static final INetworkPort TCP_11_systat = _registerPort (11, ENetworkProtocol.TCP, "systat", "Active Users");
  public static final INetworkPort TCP_13_daytime = _registerPort (13,
                                                                   ENetworkProtocol.TCP,
                                                                   "daytime",
                                                                   "Daytime (RFC 867)");
  public static final INetworkPort UDP_13_daytime = _registerPort (13,
                                                                   ENetworkProtocol.UDP,
                                                                   "daytime",
                                                                   "Daytime (RFC 867)");
  public static final INetworkPort TCP_17_qotd = _registerPort (17, ENetworkProtocol.TCP, "qotd", "Quote of the Day");
  public static final INetworkPort UDP_17_qotd = _registerPort (17, ENetworkProtocol.UDP, "qotd", "Quote of the Day");
  public static final INetworkPort TCP_18_msp = _registerPort (18, ENetworkProtocol.TCP, "msp", "Message Send Protocol");
  public static final INetworkPort UDP_18_msp = _registerPort (18, ENetworkProtocol.UDP, "msp", "Message Send Protocol");
  public static final INetworkPort TCP_19_chargen = _registerPort (19,
                                                                   ENetworkProtocol.TCP,
                                                                   "chargen",
                                                                   "Character Generator");
  public static final INetworkPort UDP_19_chargen = _registerPort (19,
                                                                   ENetworkProtocol.UDP,
                                                                   "chargen",
                                                                   "Character Generator");
  public static final INetworkPort UDP_20_ftp_data = _registerPort (20,
                                                                    ENetworkProtocol.UDP,
                                                                    "ftp-data",
                                                                    "File Transfer [Default Data]");
  public static final INetworkPort TCP_20_ftp_data = _registerPort (20,
                                                                    ENetworkProtocol.TCP,
                                                                    "ftp-data",
                                                                    "File Transfer [Default Data]");
  public static final INetworkPort UDP_21_ftp = _registerPort (21,
                                                               ENetworkProtocol.UDP,
                                                               "ftp",
                                                               "File Transfer [Control]");
  public static final INetworkPort TCP_21_ftp = _registerPort (21,
                                                               ENetworkProtocol.TCP,
                                                               "ftp",
                                                               "File Transfer [Control]");
  public static final INetworkPort TCP_22_ssh = _registerPort (22,
                                                               ENetworkProtocol.TCP,
                                                               "ssh",
                                                               "SSH Remote Login Protocol");
  public static final INetworkPort UDP_22_ssh = _registerPort (22,
                                                               ENetworkProtocol.UDP,
                                                               "ssh",
                                                               "SSH Remote Login Protocol");
  public static final INetworkPort TCP_23_telnet = _registerPort (23, ENetworkProtocol.TCP, "telnet", "Telnet");
  public static final INetworkPort UDP_23_telnet = _registerPort (23, ENetworkProtocol.UDP, "telnet", "Telnet");
  public static final INetworkPort UDP_24_ = _registerPort (24, ENetworkProtocol.UDP, "", "any private mail system");
  public static final INetworkPort TCP_24_ = _registerPort (24, ENetworkProtocol.TCP, "", "any private mail system");
  public static final INetworkPort UDP_25_smtp = _registerPort (25,
                                                                ENetworkProtocol.UDP,
                                                                "smtp",
                                                                "Simple Mail Transfer");
  public static final INetworkPort TCP_25_smtp = _registerPort (25,
                                                                ENetworkProtocol.TCP,
                                                                "smtp",
                                                                "Simple Mail Transfer");
  public static final INetworkPort TCP_27_nsw_fe = _registerPort (27,
                                                                  ENetworkProtocol.TCP,
                                                                  "nsw-fe",
                                                                  "NSW User System FE");
  public static final INetworkPort UDP_27_nsw_fe = _registerPort (27,
                                                                  ENetworkProtocol.UDP,
                                                                  "nsw-fe",
                                                                  "NSW User System FE");
  public static final INetworkPort TCP_29_msg_icp = _registerPort (29, ENetworkProtocol.TCP, "msg-icp", "MSG ICP");
  public static final INetworkPort UDP_29_msg_icp = _registerPort (29, ENetworkProtocol.UDP, "msg-icp", "MSG ICP");
  public static final INetworkPort UDP_31_msg_auth = _registerPort (31,
                                                                    ENetworkProtocol.UDP,
                                                                    "msg-auth",
                                                                    "MSG Authentication");
  public static final INetworkPort TCP_31_msg_auth = _registerPort (31,
                                                                    ENetworkProtocol.TCP,
                                                                    "msg-auth",
                                                                    "MSG Authentication");
  public static final INetworkPort TCP_33_dsp = _registerPort (33,
                                                               ENetworkProtocol.TCP,
                                                               "dsp",
                                                               "Display Support Protocol");
  public static final INetworkPort UDP_33_dsp = _registerPort (33,
                                                               ENetworkProtocol.UDP,
                                                               "dsp",
                                                               "Display Support Protocol");
  public static final INetworkPort UDP_35_ = _registerPort (35, ENetworkProtocol.UDP, "", "any private printer server");
  public static final INetworkPort TCP_35_ = _registerPort (35, ENetworkProtocol.TCP, "", "any private printer server");
  public static final INetworkPort UDP_37_time = _registerPort (37, ENetworkProtocol.UDP, "time", "Time");
  public static final INetworkPort TCP_37_time = _registerPort (37, ENetworkProtocol.TCP, "time", "Time");
  public static final INetworkPort TCP_38_rap = _registerPort (38, ENetworkProtocol.TCP, "rap", "Route Access Protocol");
  public static final INetworkPort UDP_38_rap = _registerPort (38, ENetworkProtocol.UDP, "rap", "Route Access Protocol");
  public static final INetworkPort UDP_39_rlp = _registerPort (39,
                                                               ENetworkProtocol.UDP,
                                                               "rlp",
                                                               "Resource Location Protocol");
  public static final INetworkPort TCP_39_rlp = _registerPort (39,
                                                               ENetworkProtocol.TCP,
                                                               "rlp",
                                                               "Resource Location Protocol");
  public static final INetworkPort UDP_41_graphics = _registerPort (41, ENetworkProtocol.UDP, "graphics", "Graphics");
  public static final INetworkPort TCP_41_graphics = _registerPort (41, ENetworkProtocol.TCP, "graphics", "Graphics");
  public static final INetworkPort UDP_42_name = _registerPort (42, ENetworkProtocol.UDP, "name", "Host Name Server");
  public static final INetworkPort TCP_42_name = _registerPort (42, ENetworkProtocol.TCP, "name", "Host Name Server");
  public static final INetworkPort UDP_42_nameserver = _registerPort (42,
                                                                      ENetworkProtocol.UDP,
                                                                      "nameserver",
                                                                      "Host Name Server");
  public static final INetworkPort TCP_42_nameserver = _registerPort (42,
                                                                      ENetworkProtocol.TCP,
                                                                      "nameserver",
                                                                      "Host Name Server");
  public static final INetworkPort TCP_43_nicname = _registerPort (43, ENetworkProtocol.TCP, "nicname", "Who Is");
  public static final INetworkPort UDP_43_nicname = _registerPort (43, ENetworkProtocol.UDP, "nicname", "Who Is");
  public static final INetworkPort UDP_44_mpm_flags = _registerPort (44,
                                                                     ENetworkProtocol.UDP,
                                                                     "mpm-flags",
                                                                     "MPM FLAGS Protocol");
  public static final INetworkPort TCP_44_mpm_flags = _registerPort (44,
                                                                     ENetworkProtocol.TCP,
                                                                     "mpm-flags",
                                                                     "MPM FLAGS Protocol");
  public static final INetworkPort UDP_45_mpm = _registerPort (45,
                                                               ENetworkProtocol.UDP,
                                                               "mpm",
                                                               "Message Processing Module [recv]");
  public static final INetworkPort TCP_45_mpm = _registerPort (45,
                                                               ENetworkProtocol.TCP,
                                                               "mpm",
                                                               "Message Processing Module [recv]");
  public static final INetworkPort UDP_46_mpm_snd = _registerPort (46,
                                                                   ENetworkProtocol.UDP,
                                                                   "mpm-snd",
                                                                   "MPM [default send]");
  public static final INetworkPort TCP_46_mpm_snd = _registerPort (46,
                                                                   ENetworkProtocol.TCP,
                                                                   "mpm-snd",
                                                                   "MPM [default send]");
  public static final INetworkPort TCP_47_ni_ftp = _registerPort (47, ENetworkProtocol.TCP, "ni-ftp", "NI FTP");
  public static final INetworkPort UDP_47_ni_ftp = _registerPort (47, ENetworkProtocol.UDP, "ni-ftp", "NI FTP");
  public static final INetworkPort UDP_48_auditd = _registerPort (48,
                                                                  ENetworkProtocol.UDP,
                                                                  "auditd",
                                                                  "Digital Audit Daemon");
  public static final INetworkPort TCP_48_auditd = _registerPort (48,
                                                                  ENetworkProtocol.TCP,
                                                                  "auditd",
                                                                  "Digital Audit Daemon");
  public static final INetworkPort TCP_49_tacacs = _registerPort (49,
                                                                  ENetworkProtocol.TCP,
                                                                  "tacacs",
                                                                  "Login Host Protocol (TACACS)");
  public static final INetworkPort UDP_49_tacacs = _registerPort (49,
                                                                  ENetworkProtocol.UDP,
                                                                  "tacacs",
                                                                  "Login Host Protocol (TACACS)");
  public static final INetworkPort UDP_50_re_mail_ck = _registerPort (50,
                                                                      ENetworkProtocol.UDP,
                                                                      "re-mail-ck",
                                                                      "Remote Mail Checking Protocol");
  public static final INetworkPort TCP_50_re_mail_ck = _registerPort (50,
                                                                      ENetworkProtocol.TCP,
                                                                      "re-mail-ck",
                                                                      "Remote Mail Checking Protocol");
  public static final INetworkPort UDP_51_la_maint = _registerPort (51,
                                                                    ENetworkProtocol.UDP,
                                                                    "la-maint",
                                                                    "IMP Logical Address Maintenance");
  public static final INetworkPort TCP_51_la_maint = _registerPort (51,
                                                                    ENetworkProtocol.TCP,
                                                                    "la-maint",
                                                                    "IMP Logical Address Maintenance");
  public static final INetworkPort TCP_52_xns_time = _registerPort (52,
                                                                    ENetworkProtocol.TCP,
                                                                    "xns-time",
                                                                    "XNS Time Protocol");
  public static final INetworkPort UDP_52_xns_time = _registerPort (52,
                                                                    ENetworkProtocol.UDP,
                                                                    "xns-time",
                                                                    "XNS Time Protocol");
  public static final INetworkPort TCP_53_domain = _registerPort (53,
                                                                  ENetworkProtocol.TCP,
                                                                  "domain",
                                                                  "Domain Name Server");
  public static final INetworkPort UDP_53_domain = _registerPort (53,
                                                                  ENetworkProtocol.UDP,
                                                                  "domain",
                                                                  "Domain Name Server");
  public static final INetworkPort UDP_54_xns_ch = _registerPort (54,
                                                                  ENetworkProtocol.UDP,
                                                                  "xns-ch",
                                                                  "XNS Clearinghouse");
  public static final INetworkPort TCP_54_xns_ch = _registerPort (54,
                                                                  ENetworkProtocol.TCP,
                                                                  "xns-ch",
                                                                  "XNS Clearinghouse");
  public static final INetworkPort TCP_55_isi_gl = _registerPort (55,
                                                                  ENetworkProtocol.TCP,
                                                                  "isi-gl",
                                                                  "ISI Graphics Language");
  public static final INetworkPort UDP_55_isi_gl = _registerPort (55,
                                                                  ENetworkProtocol.UDP,
                                                                  "isi-gl",
                                                                  "ISI Graphics Language");
  public static final INetworkPort TCP_56_xns_auth = _registerPort (56,
                                                                    ENetworkProtocol.TCP,
                                                                    "xns-auth",
                                                                    "XNS Authentication");
  public static final INetworkPort UDP_56_xns_auth = _registerPort (56,
                                                                    ENetworkProtocol.UDP,
                                                                    "xns-auth",
                                                                    "XNS Authentication");
  public static final INetworkPort UDP_57_ = _registerPort (57, ENetworkProtocol.UDP, "", "any private terminal access");
  public static final INetworkPort TCP_57_ = _registerPort (57, ENetworkProtocol.TCP, "", "any private terminal access");
  public static final INetworkPort UDP_58_xns_mail = _registerPort (58, ENetworkProtocol.UDP, "xns-mail", "XNS Mail");
  public static final INetworkPort TCP_58_xns_mail = _registerPort (58, ENetworkProtocol.TCP, "xns-mail", "XNS Mail");
  public static final INetworkPort UDP_59_ = _registerPort (59, ENetworkProtocol.UDP, "", "any private file service");
  public static final INetworkPort TCP_59_ = _registerPort (59, ENetworkProtocol.TCP, "", "any private file service");
  public static final INetworkPort TCP_60_ = _registerPort (60, ENetworkProtocol.TCP, "", "Unassigned");
  public static final INetworkPort UDP_60_ = _registerPort (60, ENetworkProtocol.UDP, "", "Unassigned");
  public static final INetworkPort UDP_61_ni_mail = _registerPort (61, ENetworkProtocol.UDP, "ni-mail", "NI MAIL");
  public static final INetworkPort TCP_61_ni_mail = _registerPort (61, ENetworkProtocol.TCP, "ni-mail", "NI MAIL");
  public static final INetworkPort UDP_62_acas = _registerPort (62, ENetworkProtocol.UDP, "acas", "ACA Services");
  public static final INetworkPort TCP_62_acas = _registerPort (62, ENetworkProtocol.TCP, "acas", "ACA Services");
  public static final INetworkPort TCP_63_whois = _registerPort (63, ENetworkProtocol.TCP, "whois++", "whois++");
  public static final INetworkPort UDP_63_whois = _registerPort (63, ENetworkProtocol.UDP, "whois++", "whois++");
  public static final INetworkPort TCP_64_covia = _registerPort (64,
                                                                 ENetworkProtocol.TCP,
                                                                 "covia",
                                                                 "Communications Integrator (CI)");
  public static final INetworkPort UDP_64_covia = _registerPort (64,
                                                                 ENetworkProtocol.UDP,
                                                                 "covia",
                                                                 "Communications Integrator (CI)");
  public static final INetworkPort UDP_65_tacacs_ds = _registerPort (65,
                                                                     ENetworkProtocol.UDP,
                                                                     "tacacs-ds",
                                                                     "TACACS-Database Service");
  public static final INetworkPort TCP_65_tacacs_ds = _registerPort (65,
                                                                     ENetworkProtocol.TCP,
                                                                     "tacacs-ds",
                                                                     "TACACS-Database Service");
  public static final INetworkPort TCP_66_sql_net = _registerPort (66,
                                                                   ENetworkProtocol.TCP,
                                                                   "sql*net",
                                                                   "Oracle SQL*NET");
  public static final INetworkPort UDP_66_sql_net = _registerPort (66,
                                                                   ENetworkProtocol.UDP,
                                                                   "sql*net",
                                                                   "Oracle SQL*NET");
  public static final INetworkPort TCP_67_bootps = _registerPort (67,
                                                                  ENetworkProtocol.TCP,
                                                                  "bootps",
                                                                  "Bootstrap Protocol Server");
  public static final INetworkPort UDP_67_bootps = _registerPort (67,
                                                                  ENetworkProtocol.UDP,
                                                                  "bootps",
                                                                  "Bootstrap Protocol Server");
  public static final INetworkPort TCP_68_bootpc = _registerPort (68,
                                                                  ENetworkProtocol.TCP,
                                                                  "bootpc",
                                                                  "Bootstrap Protocol Client");
  public static final INetworkPort UDP_68_bootpc = _registerPort (68,
                                                                  ENetworkProtocol.UDP,
                                                                  "bootpc",
                                                                  "Bootstrap Protocol Client");
  public static final INetworkPort TCP_69_tftp = _registerPort (69,
                                                                ENetworkProtocol.TCP,
                                                                "tftp",
                                                                "Trivial File Transfer");
  public static final INetworkPort UDP_69_tftp = _registerPort (69,
                                                                ENetworkProtocol.UDP,
                                                                "tftp",
                                                                "Trivial File Transfer");
  public static final INetworkPort UDP_70_gopher = _registerPort (70, ENetworkProtocol.UDP, "gopher", "Gopher");
  public static final INetworkPort TCP_70_gopher = _registerPort (70, ENetworkProtocol.TCP, "gopher", "Gopher");
  public static final INetworkPort UDP_71_netrjs_1 = _registerPort (71,
                                                                    ENetworkProtocol.UDP,
                                                                    "netrjs-1",
                                                                    "Remote Job Service");
  public static final INetworkPort TCP_71_netrjs_1 = _registerPort (71,
                                                                    ENetworkProtocol.TCP,
                                                                    "netrjs-1",
                                                                    "Remote Job Service");
  public static final INetworkPort TCP_72_netrjs_2 = _registerPort (72,
                                                                    ENetworkProtocol.TCP,
                                                                    "netrjs-2",
                                                                    "Remote Job Service");
  public static final INetworkPort UDP_72_netrjs_2 = _registerPort (72,
                                                                    ENetworkProtocol.UDP,
                                                                    "netrjs-2",
                                                                    "Remote Job Service");
  public static final INetworkPort TCP_73_netrjs_3 = _registerPort (73,
                                                                    ENetworkProtocol.TCP,
                                                                    "netrjs-3",
                                                                    "Remote Job Service");
  public static final INetworkPort UDP_73_netrjs_3 = _registerPort (73,
                                                                    ENetworkProtocol.UDP,
                                                                    "netrjs-3",
                                                                    "Remote Job Service");
  public static final INetworkPort TCP_74_netrjs_4 = _registerPort (74,
                                                                    ENetworkProtocol.TCP,
                                                                    "netrjs-4",
                                                                    "Remote Job Service");
  public static final INetworkPort UDP_74_netrjs_4 = _registerPort (74,
                                                                    ENetworkProtocol.UDP,
                                                                    "netrjs-4",
                                                                    "Remote Job Service");
  public static final INetworkPort UDP_75_ = _registerPort (75,
                                                            ENetworkProtocol.UDP,
                                                            "",
                                                            "any private dial out service");
  public static final INetworkPort TCP_75_ = _registerPort (75,
                                                            ENetworkProtocol.TCP,
                                                            "",
                                                            "any private dial out service");
  public static final INetworkPort UDP_76_deos = _registerPort (76,
                                                                ENetworkProtocol.UDP,
                                                                "deos",
                                                                "Distributed External Object Store");
  public static final INetworkPort TCP_76_deos = _registerPort (76,
                                                                ENetworkProtocol.TCP,
                                                                "deos",
                                                                "Distributed External Object Store");
  public static final INetworkPort UDP_77_ = _registerPort (77, ENetworkProtocol.UDP, "", "any private RJE service");
  public static final INetworkPort TCP_77_ = _registerPort (77, ENetworkProtocol.TCP, "", "any private RJE service");
  public static final INetworkPort UDP_78_vettcp = _registerPort (78, ENetworkProtocol.UDP, "vettcp", "vettcp");
  public static final INetworkPort TCP_78_vettcp = _registerPort (78, ENetworkProtocol.TCP, "vettcp", "vettcp");
  public static final INetworkPort UDP_79_finger = _registerPort (79, ENetworkProtocol.UDP, "finger", "Finger");
  public static final INetworkPort TCP_79_finger = _registerPort (79, ENetworkProtocol.TCP, "finger", "Finger");
  public static final INetworkPort TCP_80_www_http = _registerPort (80,
                                                                    ENetworkProtocol.TCP,
                                                                    "www-http",
                                                                    "World Wide Web HTTP");
  public static final INetworkPort UDP_80_www = _registerPort (80, ENetworkProtocol.UDP, "www", "World Wide Web HTTP");
  public static final INetworkPort UDP_80_http = _registerPort (80, ENetworkProtocol.UDP, "http", "World Wide Web HTTP");
  public static final INetworkPort TCP_80_www = _registerPort (80, ENetworkProtocol.TCP, "www", "World Wide Web HTTP");
  public static final INetworkPort TCP_80_http = _registerPort (80, ENetworkProtocol.TCP, "http", "World Wide Web HTTP");
  public static final INetworkPort UDP_80_www_http = _registerPort (80,
                                                                    ENetworkProtocol.UDP,
                                                                    "www-http",
                                                                    "World Wide Web HTTP");
  public static final INetworkPort UDP_82_xfer = _registerPort (82, ENetworkProtocol.UDP, "xfer", "XFER Utility");
  public static final INetworkPort TCP_82_xfer = _registerPort (82, ENetworkProtocol.TCP, "xfer", "XFER Utility");
  public static final INetworkPort UDP_83_mit_ml_dev = _registerPort (83,
                                                                      ENetworkProtocol.UDP,
                                                                      "mit-ml-dev",
                                                                      "MIT ML Device");
  public static final INetworkPort TCP_83_mit_ml_dev = _registerPort (83,
                                                                      ENetworkProtocol.TCP,
                                                                      "mit-ml-dev",
                                                                      "MIT ML Device");
  public static final INetworkPort UDP_84_ctf = _registerPort (84, ENetworkProtocol.UDP, "ctf", "Common Trace Facility");
  public static final INetworkPort TCP_84_ctf = _registerPort (84, ENetworkProtocol.TCP, "ctf", "Common Trace Facility");
  public static final INetworkPort UDP_85_mit_ml_dev = _registerPort (85,
                                                                      ENetworkProtocol.UDP,
                                                                      "mit-ml-dev",
                                                                      "MIT ML Device");
  public static final INetworkPort TCP_85_mit_ml_dev = _registerPort (85,
                                                                      ENetworkProtocol.TCP,
                                                                      "mit-ml-dev",
                                                                      "MIT ML Device");
  public static final INetworkPort TCP_86_mfcobol = _registerPort (86,
                                                                   ENetworkProtocol.TCP,
                                                                   "mfcobol",
                                                                   "Micro Focus Cobol");
  public static final INetworkPort UDP_86_mfcobol = _registerPort (86,
                                                                   ENetworkProtocol.UDP,
                                                                   "mfcobol",
                                                                   "Micro Focus Cobol");
  public static final INetworkPort UDP_87_ = _registerPort (87, ENetworkProtocol.UDP, "", "any private terminal link");
  public static final INetworkPort TCP_87_ = _registerPort (87, ENetworkProtocol.TCP, "", "any private terminal link");
  public static final INetworkPort TCP_88_kerberos = _registerPort (88, ENetworkProtocol.TCP, "kerberos", "Kerberos");
  public static final INetworkPort UDP_88_kerberos = _registerPort (88, ENetworkProtocol.UDP, "kerberos", "Kerberos");
  public static final INetworkPort TCP_89_su_mit_tg = _registerPort (89,
                                                                     ENetworkProtocol.TCP,
                                                                     "su-mit-tg",
                                                                     "SU/MIT Telnet Gateway");
  public static final INetworkPort UDP_89_su_mit_tg = _registerPort (89,
                                                                     ENetworkProtocol.UDP,
                                                                     "su-mit-tg",
                                                                     "SU/MIT Telnet Gateway");
  public static final INetworkPort TCP_90_dnsix = _registerPort (90,
                                                                 ENetworkProtocol.TCP,
                                                                 "dnsix",
                                                                 "DNSIX Securit Attribute Token Map");
  public static final INetworkPort UDP_90_dnsix = _registerPort (90,
                                                                 ENetworkProtocol.UDP,
                                                                 "dnsix",
                                                                 "DNSIX Securit Attribute Token Map");
  public static final INetworkPort TCP_91_mit_dov = _registerPort (91,
                                                                   ENetworkProtocol.TCP,
                                                                   "mit-dov",
                                                                   "MIT Dover Spooler");
  public static final INetworkPort UDP_91_mit_dov = _registerPort (91,
                                                                   ENetworkProtocol.UDP,
                                                                   "mit-dov",
                                                                   "MIT Dover Spooler");
  public static final INetworkPort UDP_92_npp = _registerPort (92,
                                                               ENetworkProtocol.UDP,
                                                               "npp",
                                                               "Network Printing Protocol");
  public static final INetworkPort TCP_92_npp = _registerPort (92,
                                                               ENetworkProtocol.TCP,
                                                               "npp",
                                                               "Network Printing Protocol");
  public static final INetworkPort UDP_93_dcp = _registerPort (93,
                                                               ENetworkProtocol.UDP,
                                                               "dcp",
                                                               "Device Control Protocol");
  public static final INetworkPort TCP_93_dcp = _registerPort (93,
                                                               ENetworkProtocol.TCP,
                                                               "dcp",
                                                               "Device Control Protocol");
  public static final INetworkPort UDP_94_objcall = _registerPort (94,
                                                                   ENetworkProtocol.UDP,
                                                                   "objcall",
                                                                   "Tivoli Object Dispatcher");
  public static final INetworkPort TCP_94_objcall = _registerPort (94,
                                                                   ENetworkProtocol.TCP,
                                                                   "objcall",
                                                                   "Tivoli Object Dispatcher");
  public static final INetworkPort UDP_95_supdup = _registerPort (95, ENetworkProtocol.UDP, "supdup", "SUPDUP");
  public static final INetworkPort TCP_95_supdup = _registerPort (95, ENetworkProtocol.TCP, "supdup", "SUPDUP");
  public static final INetworkPort TCP_96_dixie = _registerPort (96,
                                                                 ENetworkProtocol.TCP,
                                                                 "dixie",
                                                                 "DIXIE Protocol Specification");
  public static final INetworkPort UDP_96_dixie = _registerPort (96,
                                                                 ENetworkProtocol.UDP,
                                                                 "dixie",
                                                                 "DIXIE Protocol Specification");
  public static final INetworkPort UDP_97_swift_rvf = _registerPort (97,
                                                                     ENetworkProtocol.UDP,
                                                                     "swift-rvf",
                                                                     "Swift Remote Virtural File Protocol");
  public static final INetworkPort TCP_97_swift_rvf = _registerPort (97,
                                                                     ENetworkProtocol.TCP,
                                                                     "swift-rvf",
                                                                     "Swift Remote Virtural File Protocol");
  public static final INetworkPort UDP_98_tacnews = _registerPort (98, ENetworkProtocol.UDP, "tacnews", "TAC News");
  public static final INetworkPort TCP_98_tacnews = _registerPort (98, ENetworkProtocol.TCP, "tacnews", "TAC News");
  public static final INetworkPort UDP_99_metagram = _registerPort (99,
                                                                    ENetworkProtocol.UDP,
                                                                    "metagram",
                                                                    "Metagram Relay");
  public static final INetworkPort TCP_99_metagram = _registerPort (99,
                                                                    ENetworkProtocol.TCP,
                                                                    "metagram",
                                                                    "Metagram Relay");
  public static final INetworkPort TCP_100_newacct = _registerPort (100,
                                                                    ENetworkProtocol.TCP,
                                                                    "newacct",
                                                                    "[unauthorized use]");
  public static final INetworkPort UDP_101_hostname = _registerPort (101,
                                                                     ENetworkProtocol.UDP,
                                                                     "hostname",
                                                                     "NIC Host Name Server");
  public static final INetworkPort TCP_101_hostname = _registerPort (101,
                                                                     ENetworkProtocol.TCP,
                                                                     "hostname",
                                                                     "NIC Host Name Server");
  public static final INetworkPort UDP_102_iso_tsap = _registerPort (102,
                                                                     ENetworkProtocol.UDP,
                                                                     "iso-tsap",
                                                                     "ISO-TSAP Class 0");
  public static final INetworkPort TCP_102_iso_tsap = _registerPort (102,
                                                                     ENetworkProtocol.TCP,
                                                                     "iso-tsap",
                                                                     "ISO-TSAP Class 0");
  public static final INetworkPort TCP_103_gppitnp = _registerPort (103,
                                                                    ENetworkProtocol.TCP,
                                                                    "gppitnp",
                                                                    "Genesis Point-to-Point Trans Net");
  public static final INetworkPort UDP_103_gppitnp = _registerPort (103,
                                                                    ENetworkProtocol.UDP,
                                                                    "gppitnp",
                                                                    "Genesis Point-to-Point Trans Net");
  public static final INetworkPort TCP_104_acr_nema = _registerPort (104,
                                                                     ENetworkProtocol.TCP,
                                                                     "acr-nema",
                                                                     "ACR-NEMA Digital Imag. & Comm. 300");
  public static final INetworkPort UDP_104_acr_nema = _registerPort (104,
                                                                     ENetworkProtocol.UDP,
                                                                     "acr-nema",
                                                                     "ACR-NEMA Digital Imag. & Comm. 300");
  public static final INetworkPort UDP_105_csnet_ns = _registerPort (105,
                                                                     ENetworkProtocol.UDP,
                                                                     "csnet-ns",
                                                                     "Mailbox Name Nameserver");
  public static final INetworkPort TCP_105_csnet_ns = _registerPort (105,
                                                                     ENetworkProtocol.TCP,
                                                                     "csnet-ns",
                                                                     "Mailbox Name Nameserver");
  public static final INetworkPort UDP_105_cso = _registerPort (105,
                                                                ENetworkProtocol.UDP,
                                                                "cso",
                                                                "CCSO name server protocol");
  public static final INetworkPort TCP_105_cso = _registerPort (105,
                                                                ENetworkProtocol.TCP,
                                                                "cso",
                                                                "CCSO name server protocol");
  public static final INetworkPort UDP_106_3com_tsmux = _registerPort (106,
                                                                       ENetworkProtocol.UDP,
                                                                       "3com-tsmux",
                                                                       "3COM-TSMUX");
  public static final INetworkPort TCP_106_3com_tsmux = _registerPort (106,
                                                                       ENetworkProtocol.TCP,
                                                                       "3com-tsmux",
                                                                       "3COM-TSMUX");
  public static final INetworkPort TCP_107_rtelnet = _registerPort (107,
                                                                    ENetworkProtocol.TCP,
                                                                    "rtelnet",
                                                                    "Remote Telnet Service");
  public static final INetworkPort UDP_107_rtelnet = _registerPort (107,
                                                                    ENetworkProtocol.UDP,
                                                                    "rtelnet",
                                                                    "Remote Telnet Service");
  public static final INetworkPort TCP_108_snagas = _registerPort (108,
                                                                   ENetworkProtocol.TCP,
                                                                   "snagas",
                                                                   "SNA Gateway Access Server");
  public static final INetworkPort UDP_108_snagas = _registerPort (108,
                                                                   ENetworkProtocol.UDP,
                                                                   "snagas",
                                                                   "SNA Gateway Access Server");
  public static final INetworkPort TCP_109_pop2 = _registerPort (109,
                                                                 ENetworkProtocol.TCP,
                                                                 "pop2",
                                                                 "Post Office Protocol - Version 2");
  public static final INetworkPort UDP_109_pop2 = _registerPort (109,
                                                                 ENetworkProtocol.UDP,
                                                                 "pop2",
                                                                 "Post Office Protocol - Version 2");
  public static final INetworkPort TCP_110_pop3 = _registerPort (110,
                                                                 ENetworkProtocol.TCP,
                                                                 "pop3",
                                                                 "Post Office Protocol - Version 3");
  public static final INetworkPort UDP_110_pop3 = _registerPort (110,
                                                                 ENetworkProtocol.UDP,
                                                                 "pop3",
                                                                 "Post Office Protocol - Version 3");
  public static final INetworkPort UDP_111_sunrpc = _registerPort (111,
                                                                   ENetworkProtocol.UDP,
                                                                   "sunrpc",
                                                                   "SUN Remote Procedure Call");
  public static final INetworkPort TCP_111_sunrpc = _registerPort (111,
                                                                   ENetworkProtocol.TCP,
                                                                   "sunrpc",
                                                                   "SUN Remote Procedure Call");
  public static final INetworkPort TCP_112_mcidas = _registerPort (112,
                                                                   ENetworkProtocol.TCP,
                                                                   "mcidas",
                                                                   "McIDAS Data Transmission Protocol");
  public static final INetworkPort UDP_112_mcidas = _registerPort (112,
                                                                   ENetworkProtocol.UDP,
                                                                   "mcidas",
                                                                   "McIDAS Data Transmission Protocol");
  public static final INetworkPort TCP_113_auth = _registerPort (113,
                                                                 ENetworkProtocol.TCP,
                                                                 "auth",
                                                                 "Authentication Service");
  public static final INetworkPort UDP_113_auth = _registerPort (113,
                                                                 ENetworkProtocol.UDP,
                                                                 "auth",
                                                                 "Authentication Service");
  public static final INetworkPort TCP_113_ident = _registerPort (113, ENetworkProtocol.TCP, "ident", "");
  public static final INetworkPort UDP_115_sftp = _registerPort (115,
                                                                 ENetworkProtocol.UDP,
                                                                 "sftp",
                                                                 "Simple File Transfer Protocol");
  public static final INetworkPort TCP_115_sftp = _registerPort (115,
                                                                 ENetworkProtocol.TCP,
                                                                 "sftp",
                                                                 "Simple File Transfer Protocol");
  public static final INetworkPort TCP_116_ansanotify = _registerPort (116,
                                                                       ENetworkProtocol.TCP,
                                                                       "ansanotify",
                                                                       "ANSA REX Notify");
  public static final INetworkPort UDP_116_ansanotify = _registerPort (116,
                                                                       ENetworkProtocol.UDP,
                                                                       "ansanotify",
                                                                       "ANSA REX Notify");
  public static final INetworkPort UDP_117_uucp_path = _registerPort (117,
                                                                      ENetworkProtocol.UDP,
                                                                      "uucp-path",
                                                                      "UUCP Path Service");
  public static final INetworkPort TCP_117_uucp_path = _registerPort (117,
                                                                      ENetworkProtocol.TCP,
                                                                      "uucp-path",
                                                                      "UUCP Path Service");
  public static final INetworkPort TCP_118_sqlserv = _registerPort (118,
                                                                    ENetworkProtocol.TCP,
                                                                    "sqlserv",
                                                                    "SQL Services");
  public static final INetworkPort UDP_118_sqlserv = _registerPort (118,
                                                                    ENetworkProtocol.UDP,
                                                                    "sqlserv",
                                                                    "SQL Services");
  public static final INetworkPort TCP_119_nntp = _registerPort (119,
                                                                 ENetworkProtocol.TCP,
                                                                 "nntp",
                                                                 "Network News Transfer Protocol");
  public static final INetworkPort UDP_119_nntp = _registerPort (119,
                                                                 ENetworkProtocol.UDP,
                                                                 "nntp",
                                                                 "Network News Transfer Protocol");
  public static final INetworkPort UDP_120_cfdptkt = _registerPort (120, ENetworkProtocol.UDP, "cfdptkt", "CFDPTKT");
  public static final INetworkPort TCP_120_cfdptkt = _registerPort (120, ENetworkProtocol.TCP, "cfdptkt", "CFDPTKT");
  public static final INetworkPort TCP_121_erpc = _registerPort (121,
                                                                 ENetworkProtocol.TCP,
                                                                 "erpc",
                                                                 "Encore Expedited Remote Pro.Call");
  public static final INetworkPort UDP_121_erpc = _registerPort (121,
                                                                 ENetworkProtocol.UDP,
                                                                 "erpc",
                                                                 "Encore Expedited Remote Pro.Call");
  public static final INetworkPort TCP_122_smakynet = _registerPort (122, ENetworkProtocol.TCP, "smakynet", "SMAKYNET");
  public static final INetworkPort UDP_122_smakynet = _registerPort (122, ENetworkProtocol.UDP, "smakynet", "SMAKYNET");
  public static final INetworkPort UDP_123_ntp = _registerPort (123,
                                                                ENetworkProtocol.UDP,
                                                                "ntp",
                                                                "Network Time Protocol");
  public static final INetworkPort TCP_123_ntp = _registerPort (123,
                                                                ENetworkProtocol.TCP,
                                                                "ntp",
                                                                "Network Time Protocol");
  public static final INetworkPort UDP_124_ansatrader = _registerPort (124,
                                                                       ENetworkProtocol.UDP,
                                                                       "ansatrader",
                                                                       "ANSA REX Trader");
  public static final INetworkPort TCP_124_ansatrader = _registerPort (124,
                                                                       ENetworkProtocol.TCP,
                                                                       "ansatrader",
                                                                       "ANSA REX Trader");
  public static final INetworkPort UDP_125_locus_map = _registerPort (125,
                                                                      ENetworkProtocol.UDP,
                                                                      "locus-map",
                                                                      "Locus PC-Interface Net Map Ser");
  public static final INetworkPort TCP_125_locus_map = _registerPort (125,
                                                                      ENetworkProtocol.TCP,
                                                                      "locus-map",
                                                                      "Locus PC-Interface Net Map Ser");
  public static final INetworkPort TCP_126_nxedit = _registerPort (126, ENetworkProtocol.TCP, "nxedit", "NXEdit");
  public static final INetworkPort UDP_126_nxedit = _registerPort (126, ENetworkProtocol.UDP, "nxedit", "NXEdit");
  public static final INetworkPort TCP_127_locus_con = _registerPort (127,
                                                                      ENetworkProtocol.TCP,
                                                                      "locus-con",
                                                                      "Locus PC-Interface Conn Server");
  public static final INetworkPort UDP_127_locus_con = _registerPort (127,
                                                                      ENetworkProtocol.UDP,
                                                                      "locus-con",
                                                                      "Locus PC-Interface Conn Server");
  public static final INetworkPort TCP_128_gss_xlicen = _registerPort (128,
                                                                       ENetworkProtocol.TCP,
                                                                       "gss-xlicen",
                                                                       "GSS X License Verification");
  public static final INetworkPort UDP_128_gss_xlicen = _registerPort (128,
                                                                       ENetworkProtocol.UDP,
                                                                       "gss-xlicen",
                                                                       "GSS X License Verification");
  public static final INetworkPort TCP_129_pwdgen = _registerPort (129,
                                                                   ENetworkProtocol.TCP,
                                                                   "pwdgen",
                                                                   "Password Generator Protocol");
  public static final INetworkPort UDP_129_pwdgen = _registerPort (129,
                                                                   ENetworkProtocol.UDP,
                                                                   "pwdgen",
                                                                   "Password Generator Protocol");
  public static final INetworkPort UDP_130_cisco_fna = _registerPort (130,
                                                                      ENetworkProtocol.UDP,
                                                                      "cisco-fna",
                                                                      "cisco FNATIVE");
  public static final INetworkPort TCP_130_cisco_fna = _registerPort (130,
                                                                      ENetworkProtocol.TCP,
                                                                      "cisco-fna",
                                                                      "cisco FNATIVE");
  public static final INetworkPort UDP_131_cisco_tna = _registerPort (131,
                                                                      ENetworkProtocol.UDP,
                                                                      "cisco-tna",
                                                                      "cisco TNATIVE");
  public static final INetworkPort TCP_131_cisco_tna = _registerPort (131,
                                                                      ENetworkProtocol.TCP,
                                                                      "cisco-tna",
                                                                      "cisco TNATIVE");
  public static final INetworkPort UDP_132_cisco_sys = _registerPort (132,
                                                                      ENetworkProtocol.UDP,
                                                                      "cisco-sys",
                                                                      "cisco SYSMAINT");
  public static final INetworkPort TCP_132_cisco_sys = _registerPort (132,
                                                                      ENetworkProtocol.TCP,
                                                                      "cisco-sys",
                                                                      "cisco SYSMAINT");
  public static final INetworkPort TCP_133_statsrv = _registerPort (133,
                                                                    ENetworkProtocol.TCP,
                                                                    "statsrv",
                                                                    "Statistics Service");
  public static final INetworkPort UDP_133_statsrv = _registerPort (133,
                                                                    ENetworkProtocol.UDP,
                                                                    "statsrv",
                                                                    "Statistics Service");
  public static final INetworkPort TCP_134_ingres_net = _registerPort (134,
                                                                       ENetworkProtocol.TCP,
                                                                       "ingres-net",
                                                                       "INGRES-NET Service");
  public static final INetworkPort UDP_134_ingres_net = _registerPort (134,
                                                                       ENetworkProtocol.UDP,
                                                                       "ingres-net",
                                                                       "INGRES-NET Service");
  public static final INetworkPort TCP_135_epmap = _registerPort (135,
                                                                  ENetworkProtocol.TCP,
                                                                  "epmap",
                                                                  "DCE endpoint resolution");
  public static final INetworkPort UDP_135_epmap = _registerPort (135,
                                                                  ENetworkProtocol.UDP,
                                                                  "epmap",
                                                                  "DCE endpoint resolution");
  public static final INetworkPort UDP_136_profile = _registerPort (136,
                                                                    ENetworkProtocol.UDP,
                                                                    "profile",
                                                                    "PROFILE Naming System");
  public static final INetworkPort TCP_136_profile = _registerPort (136,
                                                                    ENetworkProtocol.TCP,
                                                                    "profile",
                                                                    "PROFILE Naming System");
  public static final INetworkPort TCP_137_netbios_ns = _registerPort (137,
                                                                       ENetworkProtocol.TCP,
                                                                       "netbios-ns",
                                                                       "NETBIOS Name Service");
  public static final INetworkPort UDP_137_netbios_ns = _registerPort (137,
                                                                       ENetworkProtocol.UDP,
                                                                       "netbios-ns",
                                                                       "NETBIOS Name Service");
  public static final INetworkPort UDP_138_netbios_dgm = _registerPort (138,
                                                                        ENetworkProtocol.UDP,
                                                                        "netbios-dgm",
                                                                        "NETBIOS Datagram Service");
  public static final INetworkPort TCP_138_netbios_dgm = _registerPort (138,
                                                                        ENetworkProtocol.TCP,
                                                                        "netbios-dgm",
                                                                        "NETBIOS Datagram Service");
  public static final INetworkPort UDP_139_netbios_ssn = _registerPort (139,
                                                                        ENetworkProtocol.UDP,
                                                                        "netbios-ssn",
                                                                        "NETBIOS Session Service");
  public static final INetworkPort TCP_139_netbios_ssn = _registerPort (139,
                                                                        ENetworkProtocol.TCP,
                                                                        "netbios-ssn",
                                                                        "NETBIOS Session Service");
  public static final INetworkPort UDP_140_emfis_data = _registerPort (140,
                                                                       ENetworkProtocol.UDP,
                                                                       "emfis-data",
                                                                       "EMFIS Data Service");
  public static final INetworkPort TCP_140_emfis_data = _registerPort (140,
                                                                       ENetworkProtocol.TCP,
                                                                       "emfis-data",
                                                                       "EMFIS Data Service");
  public static final INetworkPort UDP_141_emfis_cntl = _registerPort (141,
                                                                       ENetworkProtocol.UDP,
                                                                       "emfis-cntl",
                                                                       "EMFIS Control Service");
  public static final INetworkPort TCP_141_emfis_cntl = _registerPort (141,
                                                                       ENetworkProtocol.TCP,
                                                                       "emfis-cntl",
                                                                       "EMFIS Control Service");
  public static final INetworkPort TCP_142_bl_idm = _registerPort (142,
                                                                   ENetworkProtocol.TCP,
                                                                   "bl-idm",
                                                                   "Britton-Lee IDM");
  public static final INetworkPort UDP_142_bl_idm = _registerPort (142,
                                                                   ENetworkProtocol.UDP,
                                                                   "bl-idm",
                                                                   "Britton-Lee IDM");
  public static final INetworkPort UDP_143_imap = _registerPort (143,
                                                                 ENetworkProtocol.UDP,
                                                                 "imap",
                                                                 "Internet Message Access Protocol");
  public static final INetworkPort TCP_143_imap = _registerPort (143,
                                                                 ENetworkProtocol.TCP,
                                                                 "imap",
                                                                 "Internet Message Access Protocol");
  public static final INetworkPort UDP_144_uma = _registerPort (144,
                                                                ENetworkProtocol.UDP,
                                                                "uma",
                                                                "Universal Management Architecture");
  public static final INetworkPort TCP_144_uma = _registerPort (144,
                                                                ENetworkProtocol.TCP,
                                                                "uma",
                                                                "Universal Management Architecture");
  public static final INetworkPort TCP_145_uaac = _registerPort (145, ENetworkProtocol.TCP, "uaac", "UAAC Protocol");
  public static final INetworkPort UDP_145_uaac = _registerPort (145, ENetworkProtocol.UDP, "uaac", "UAAC Protocol");
  public static final INetworkPort UDP_146_iso_tp0 = _registerPort (146, ENetworkProtocol.UDP, "iso-tp0", "ISO-IP0");
  public static final INetworkPort TCP_146_iso_tp0 = _registerPort (146, ENetworkProtocol.TCP, "iso-tp0", "ISO-IP0");
  public static final INetworkPort UDP_147_iso_ip = _registerPort (147, ENetworkProtocol.UDP, "iso-ip", "ISO-IP");
  public static final INetworkPort TCP_147_iso_ip = _registerPort (147, ENetworkProtocol.TCP, "iso-ip", "ISO-IP");
  public static final INetworkPort TCP_148_jargon = _registerPort (148, ENetworkProtocol.TCP, "jargon", "Jargon");
  public static final INetworkPort UDP_148_jargon = _registerPort (148, ENetworkProtocol.UDP, "jargon", "Jargon");
  public static final INetworkPort UDP_149_aed_512 = _registerPort (149,
                                                                    ENetworkProtocol.UDP,
                                                                    "aed-512",
                                                                    "AED 512 Emulation Service");
  public static final INetworkPort TCP_149_aed_512 = _registerPort (149,
                                                                    ENetworkProtocol.TCP,
                                                                    "aed-512",
                                                                    "AED 512 Emulation Service");
  public static final INetworkPort TCP_150_sql_net = _registerPort (150, ENetworkProtocol.TCP, "sql-net", "SQL-NET");
  public static final INetworkPort UDP_150_sql_net = _registerPort (150, ENetworkProtocol.UDP, "sql-net", "SQL-NET");
  public static final INetworkPort TCP_151_hems = _registerPort (151, ENetworkProtocol.TCP, "hems", "HEMS");
  public static final INetworkPort UDP_151_hems = _registerPort (151, ENetworkProtocol.UDP, "hems", "HEMS");
  public static final INetworkPort TCP_152_bftp = _registerPort (152,
                                                                 ENetworkProtocol.TCP,
                                                                 "bftp",
                                                                 "Background File Transfer Program");
  public static final INetworkPort UDP_152_bftp = _registerPort (152,
                                                                 ENetworkProtocol.UDP,
                                                                 "bftp",
                                                                 "Background File Transfer Program");
  public static final INetworkPort UDP_153_sgmp = _registerPort (153, ENetworkProtocol.UDP, "sgmp", "SGMP");
  public static final INetworkPort TCP_153_sgmp = _registerPort (153, ENetworkProtocol.TCP, "sgmp", "SGMP");
  public static final INetworkPort TCP_154_netsc_prod = _registerPort (154, ENetworkProtocol.TCP, "netsc-prod", "NETSC");
  public static final INetworkPort UDP_154_netsc_prod = _registerPort (154, ENetworkProtocol.UDP, "netsc-prod", "NETSC");
  public static final INetworkPort TCP_155_netsc_dev = _registerPort (155, ENetworkProtocol.TCP, "netsc-dev", "NETSC");
  public static final INetworkPort UDP_155_netsc_dev = _registerPort (155, ENetworkProtocol.UDP, "netsc-dev", "NETSC");
  public static final INetworkPort TCP_156_sqlsrv = _registerPort (156, ENetworkProtocol.TCP, "sqlsrv", "SQL Service");
  public static final INetworkPort UDP_156_sqlsrv = _registerPort (156, ENetworkProtocol.UDP, "sqlsrv", "SQL Service");
  public static final INetworkPort TCP_157_knet_cmp = _registerPort (157,
                                                                     ENetworkProtocol.TCP,
                                                                     "knet-cmp",
                                                                     "KNET/VM Command/Message Protocol");
  public static final INetworkPort UDP_157_knet_cmp = _registerPort (157,
                                                                     ENetworkProtocol.UDP,
                                                                     "knet-cmp",
                                                                     "KNET/VM Command/Message Protocol");
  public static final INetworkPort TCP_158_pcmail_srv = _registerPort (158,
                                                                       ENetworkProtocol.TCP,
                                                                       "pcmail-srv",
                                                                       "PCMail Server");
  public static final INetworkPort UDP_158_pcmail_srv = _registerPort (158,
                                                                       ENetworkProtocol.UDP,
                                                                       "pcmail-srv",
                                                                       "PCMail Server");
  public static final INetworkPort UDP_159_nss_routing = _registerPort (159,
                                                                        ENetworkProtocol.UDP,
                                                                        "nss-routing",
                                                                        "NSS-Routing");
  public static final INetworkPort TCP_159_nss_routing = _registerPort (159,
                                                                        ENetworkProtocol.TCP,
                                                                        "nss-routing",
                                                                        "NSS-Routing");
  public static final INetworkPort UDP_160_sgmp_traps = _registerPort (160,
                                                                       ENetworkProtocol.UDP,
                                                                       "sgmp-traps",
                                                                       "SGMP-TRAPS");
  public static final INetworkPort TCP_160_sgmp_traps = _registerPort (160,
                                                                       ENetworkProtocol.TCP,
                                                                       "sgmp-traps",
                                                                       "SGMP-TRAPS");
  public static final INetworkPort TCP_161_snmp = _registerPort (161, ENetworkProtocol.TCP, "snmp", "SNMP");
  public static final INetworkPort UDP_161_snmp = _registerPort (161, ENetworkProtocol.UDP, "snmp", "SNMP");
  public static final INetworkPort TCP_162_snmptrap = _registerPort (162, ENetworkProtocol.TCP, "snmptrap", "SNMPTRAP");
  public static final INetworkPort UDP_162_snmptrap = _registerPort (162, ENetworkProtocol.UDP, "snmptrap", "SNMPTRAP");
  public static final INetworkPort TCP_163_cmip_man = _registerPort (163,
                                                                     ENetworkProtocol.TCP,
                                                                     "cmip-man",
                                                                     "CMIP/TCP Manager");
  public static final INetworkPort UDP_163_cmip_man = _registerPort (163,
                                                                     ENetworkProtocol.UDP,
                                                                     "cmip-man",
                                                                     "CMIP/TCP Manager");
  public static final INetworkPort UDP_164_cmip_agent = _registerPort (164,
                                                                       ENetworkProtocol.UDP,
                                                                       "cmip-agent",
                                                                       "CMIP/TCP Agent");
  public static final INetworkPort TCP_164_cmip_agent = _registerPort (164,
                                                                       ENetworkProtocol.TCP,
                                                                       "cmip-agent",
                                                                       "CMIP/TCP Agent");
  public static final INetworkPort TCP_165_xns_courier = _registerPort (165,
                                                                        ENetworkProtocol.TCP,
                                                                        "xns-courier",
                                                                        "Xerox");
  public static final INetworkPort UDP_165_xns_courier = _registerPort (165,
                                                                        ENetworkProtocol.UDP,
                                                                        "xns-courier",
                                                                        "Xerox");
  public static final INetworkPort UDP_166_s_net = _registerPort (166, ENetworkProtocol.UDP, "s-net", "Sirius Systems");
  public static final INetworkPort TCP_166_s_net = _registerPort (166, ENetworkProtocol.TCP, "s-net", "Sirius Systems");
  public static final INetworkPort UDP_167_namp = _registerPort (167, ENetworkProtocol.UDP, "namp", "NAMP");
  public static final INetworkPort TCP_167_namp = _registerPort (167, ENetworkProtocol.TCP, "namp", "NAMP");
  public static final INetworkPort UDP_168_rsvd = _registerPort (168, ENetworkProtocol.UDP, "rsvd", "RSVD");
  public static final INetworkPort TCP_168_rsvd = _registerPort (168, ENetworkProtocol.TCP, "rsvd", "RSVD");
  public static final INetworkPort UDP_169_send = _registerPort (169, ENetworkProtocol.UDP, "send", "SEND");
  public static final INetworkPort TCP_169_send = _registerPort (169, ENetworkProtocol.TCP, "send", "SEND");
  public static final INetworkPort UDP_170_print_srv = _registerPort (170,
                                                                      ENetworkProtocol.UDP,
                                                                      "print-srv",
                                                                      "Network PostScript");
  public static final INetworkPort TCP_170_print_srv = _registerPort (170,
                                                                      ENetworkProtocol.TCP,
                                                                      "print-srv",
                                                                      "Network PostScript");
  public static final INetworkPort TCP_171_multiplex = _registerPort (171,
                                                                      ENetworkProtocol.TCP,
                                                                      "multiplex",
                                                                      "Network Innovations Multiplex");
  public static final INetworkPort UDP_171_multiplex = _registerPort (171,
                                                                      ENetworkProtocol.UDP,
                                                                      "multiplex",
                                                                      "Network Innovations Multiplex");
  public static final INetworkPort TCP_172_cl_1 = _registerPort (172,
                                                                 ENetworkProtocol.TCP,
                                                                 "cl/1",
                                                                 "Network Innovations CL/1");
  public static final INetworkPort UDP_172_cl_1 = _registerPort (172,
                                                                 ENetworkProtocol.UDP,
                                                                 "cl/1",
                                                                 "Network Innovations CL/1");
  public static final INetworkPort UDP_173_xyplex_mux = _registerPort (173,
                                                                       ENetworkProtocol.UDP,
                                                                       "xyplex-mux",
                                                                       "Xyplex");
  public static final INetworkPort TCP_173_xyplex_mux = _registerPort (173,
                                                                       ENetworkProtocol.TCP,
                                                                       "xyplex-mux",
                                                                       "Xyplex");
  public static final INetworkPort UDP_174_mailq = _registerPort (174, ENetworkProtocol.UDP, "mailq", "MAILQ");
  public static final INetworkPort TCP_174_mailq = _registerPort (174, ENetworkProtocol.TCP, "mailq", "MAILQ");
  public static final INetworkPort TCP_175_vmnet = _registerPort (175, ENetworkProtocol.TCP, "vmnet", "VMNET");
  public static final INetworkPort UDP_175_vmnet = _registerPort (175, ENetworkProtocol.UDP, "vmnet", "VMNET");
  public static final INetworkPort TCP_176_genrad_mux = _registerPort (176,
                                                                       ENetworkProtocol.TCP,
                                                                       "genrad-mux",
                                                                       "GENRAD-MUX");
  public static final INetworkPort UDP_176_genrad_mux = _registerPort (176,
                                                                       ENetworkProtocol.UDP,
                                                                       "genrad-mux",
                                                                       "GENRAD-MUX");
  public static final INetworkPort UDP_177_xdmcp = _registerPort (177,
                                                                  ENetworkProtocol.UDP,
                                                                  "xdmcp",
                                                                  "X Display Manager Control Protocol");
  public static final INetworkPort TCP_177_xdmcp = _registerPort (177,
                                                                  ENetworkProtocol.TCP,
                                                                  "xdmcp",
                                                                  "X Display Manager Control Protocol");
  public static final INetworkPort UDP_178_nextstep = _registerPort (178,
                                                                     ENetworkProtocol.UDP,
                                                                     "nextstep",
                                                                     "NextStep Window Server");
  public static final INetworkPort TCP_178_nextstep = _registerPort (178,
                                                                     ENetworkProtocol.TCP,
                                                                     "nextstep",
                                                                     "NextStep Window Server");
  public static final INetworkPort TCP_179_bgp = _registerPort (179,
                                                                ENetworkProtocol.TCP,
                                                                "bgp",
                                                                "Border Gateway Protocol");
  public static final INetworkPort UDP_179_bgp = _registerPort (179,
                                                                ENetworkProtocol.UDP,
                                                                "bgp",
                                                                "Border Gateway Protocol");
  public static final INetworkPort UDP_180_ris = _registerPort (180, ENetworkProtocol.UDP, "ris", "Intergraph");
  public static final INetworkPort TCP_180_ris = _registerPort (180, ENetworkProtocol.TCP, "ris", "Intergraph");
  public static final INetworkPort UDP_181_unify = _registerPort (181, ENetworkProtocol.UDP, "unify", "Unify");
  public static final INetworkPort TCP_181_unify = _registerPort (181, ENetworkProtocol.TCP, "unify", "Unify");
  public static final INetworkPort UDP_182_audit = _registerPort (182,
                                                                  ENetworkProtocol.UDP,
                                                                  "audit",
                                                                  "Unisys Audit SITP");
  public static final INetworkPort TCP_182_audit = _registerPort (182,
                                                                  ENetworkProtocol.TCP,
                                                                  "audit",
                                                                  "Unisys Audit SITP");
  public static final INetworkPort TCP_183_ocbinder = _registerPort (183, ENetworkProtocol.TCP, "ocbinder", "OCBinder");
  public static final INetworkPort UDP_183_ocbinder = _registerPort (183, ENetworkProtocol.UDP, "ocbinder", "OCBinder");
  public static final INetworkPort UDP_184_ocserver = _registerPort (184, ENetworkProtocol.UDP, "ocserver", "OCServer");
  public static final INetworkPort TCP_184_ocserver = _registerPort (184, ENetworkProtocol.TCP, "ocserver", "OCServer");
  public static final INetworkPort UDP_185_remote_kis = _registerPort (185,
                                                                       ENetworkProtocol.UDP,
                                                                       "remote-kis",
                                                                       "Remote-KIS");
  public static final INetworkPort TCP_185_remote_kis = _registerPort (185,
                                                                       ENetworkProtocol.TCP,
                                                                       "remote-kis",
                                                                       "Remote-KIS");
  public static final INetworkPort UDP_186_kis = _registerPort (186, ENetworkProtocol.UDP, "kis", "KIS Protocol");
  public static final INetworkPort TCP_186_kis = _registerPort (186, ENetworkProtocol.TCP, "kis", "KIS Protocol");
  public static final INetworkPort UDP_187_aci = _registerPort (187,
                                                                ENetworkProtocol.UDP,
                                                                "aci",
                                                                "Application Communication Interface");
  public static final INetworkPort TCP_187_aci = _registerPort (187,
                                                                ENetworkProtocol.TCP,
                                                                "aci",
                                                                "Application Communication Interface");
  public static final INetworkPort UDP_188_mumps = _registerPort (188,
                                                                  ENetworkProtocol.UDP,
                                                                  "mumps",
                                                                  "Plus Five's MUMPS");
  public static final INetworkPort TCP_188_mumps = _registerPort (188,
                                                                  ENetworkProtocol.TCP,
                                                                  "mumps",
                                                                  "Plus Five's MUMPS");
  public static final INetworkPort UDP_189_qft = _registerPort (189,
                                                                ENetworkProtocol.UDP,
                                                                "qft",
                                                                "Queued File Transport");
  public static final INetworkPort TCP_189_qft = _registerPort (189,
                                                                ENetworkProtocol.TCP,
                                                                "qft",
                                                                "Queued File Transport");
  public static final INetworkPort TCP_190_gacp = _registerPort (190,
                                                                 ENetworkProtocol.TCP,
                                                                 "gacp",
                                                                 "Gateway Access Control Protocol");
  public static final INetworkPort UDP_190_gacp = _registerPort (190,
                                                                 ENetworkProtocol.UDP,
                                                                 "gacp",
                                                                 "Gateway Access Control Protocol");
  public static final INetworkPort TCP_191_prospero = _registerPort (191,
                                                                     ENetworkProtocol.TCP,
                                                                     "prospero",
                                                                     "Prospero Directory Service");
  public static final INetworkPort UDP_191_prospero = _registerPort (191,
                                                                     ENetworkProtocol.UDP,
                                                                     "prospero",
                                                                     "Prospero Directory Service");
  public static final INetworkPort TCP_192_osu_nms = _registerPort (192,
                                                                    ENetworkProtocol.TCP,
                                                                    "osu-nms",
                                                                    "OSU Network Monitoring System");
  public static final INetworkPort UDP_192_osu_nms = _registerPort (192,
                                                                    ENetworkProtocol.UDP,
                                                                    "osu-nms",
                                                                    "OSU Network Monitoring System");
  public static final INetworkPort UDP_193_srmp = _registerPort (193,
                                                                 ENetworkProtocol.UDP,
                                                                 "srmp",
                                                                 "Spider Remote Monitoring Protocol");
  public static final INetworkPort TCP_193_srmp = _registerPort (193,
                                                                 ENetworkProtocol.TCP,
                                                                 "srmp",
                                                                 "Spider Remote Monitoring Protocol");
  public static final INetworkPort TCP_194_irc = _registerPort (194,
                                                                ENetworkProtocol.TCP,
                                                                "irc",
                                                                "Internet Relay Chat Protocol");
  public static final INetworkPort UDP_194_irc = _registerPort (194,
                                                                ENetworkProtocol.UDP,
                                                                "irc",
                                                                "Internet Relay Chat Protocol");
  public static final INetworkPort UDP_195_dn6_nlm_aud = _registerPort (195,
                                                                        ENetworkProtocol.UDP,
                                                                        "dn6-nlm-aud",
                                                                        "DNSIX Network Level Module Audit");
  public static final INetworkPort TCP_195_dn6_nlm_aud = _registerPort (195,
                                                                        ENetworkProtocol.TCP,
                                                                        "dn6-nlm-aud",
                                                                        "DNSIX Network Level Module Audit");
  public static final INetworkPort TCP_196_dn6_smm_red = _registerPort (196,
                                                                        ENetworkProtocol.TCP,
                                                                        "dn6-smm-red",
                                                                        "DNSIX Session Mgt Module Audit Redir");
  public static final INetworkPort UDP_196_dn6_smm_red = _registerPort (196,
                                                                        ENetworkProtocol.UDP,
                                                                        "dn6-smm-red",
                                                                        "DNSIX Session Mgt Module Audit Redir");
  public static final INetworkPort UDP_197_dls = _registerPort (197,
                                                                ENetworkProtocol.UDP,
                                                                "dls",
                                                                "Directory Location Service");
  public static final INetworkPort TCP_197_dls = _registerPort (197,
                                                                ENetworkProtocol.TCP,
                                                                "dls",
                                                                "Directory Location Service");
  public static final INetworkPort UDP_198_dls_mon = _registerPort (198,
                                                                    ENetworkProtocol.UDP,
                                                                    "dls-mon",
                                                                    "Directory Location Service Monitor");
  public static final INetworkPort TCP_198_dls_mon = _registerPort (198,
                                                                    ENetworkProtocol.TCP,
                                                                    "dls-mon",
                                                                    "Directory Location Service Monitor");
  public static final INetworkPort TCP_199_smux = _registerPort (199, ENetworkProtocol.TCP, "smux", "SMUX");
  public static final INetworkPort UDP_199_smux = _registerPort (199, ENetworkProtocol.UDP, "smux", "SMUX");
  public static final INetworkPort TCP_200_src = _registerPort (200,
                                                                ENetworkProtocol.TCP,
                                                                "src",
                                                                "IBM System Resource Controller");
  public static final INetworkPort UDP_200_src = _registerPort (200,
                                                                ENetworkProtocol.UDP,
                                                                "src",
                                                                "IBM System Resource Controller");
  public static final INetworkPort TCP_201_at_rtmp = _registerPort (201,
                                                                    ENetworkProtocol.TCP,
                                                                    "at-rtmp",
                                                                    "AppleTalk Routing Maintenance");
  public static final INetworkPort UDP_201_at_rtmp = _registerPort (201,
                                                                    ENetworkProtocol.UDP,
                                                                    "at-rtmp",
                                                                    "AppleTalk Routing Maintenance");
  public static final INetworkPort UDP_202_at_nbp = _registerPort (202,
                                                                   ENetworkProtocol.UDP,
                                                                   "at-nbp",
                                                                   "AppleTalk Name Binding");
  public static final INetworkPort TCP_202_at_nbp = _registerPort (202,
                                                                   ENetworkProtocol.TCP,
                                                                   "at-nbp",
                                                                   "AppleTalk Name Binding");
  public static final INetworkPort UDP_203_at_3 = _registerPort (203, ENetworkProtocol.UDP, "at-3", "AppleTalk Unused");
  public static final INetworkPort TCP_203_at_3 = _registerPort (203, ENetworkProtocol.TCP, "at-3", "AppleTalk Unused");
  public static final INetworkPort UDP_204_at_echo = _registerPort (204,
                                                                    ENetworkProtocol.UDP,
                                                                    "at-echo",
                                                                    "AppleTalk Echo");
  public static final INetworkPort TCP_204_at_echo = _registerPort (204,
                                                                    ENetworkProtocol.TCP,
                                                                    "at-echo",
                                                                    "AppleTalk Echo");
  public static final INetworkPort TCP_205_at_5 = _registerPort (205, ENetworkProtocol.TCP, "at-5", "AppleTalk Unused");
  public static final INetworkPort UDP_205_at_5 = _registerPort (205, ENetworkProtocol.UDP, "at-5", "AppleTalk Unused");
  public static final INetworkPort UDP_206_at_zis = _registerPort (206,
                                                                   ENetworkProtocol.UDP,
                                                                   "at-zis",
                                                                   "AppleTalk Zone Information");
  public static final INetworkPort TCP_206_at_zis = _registerPort (206,
                                                                   ENetworkProtocol.TCP,
                                                                   "at-zis",
                                                                   "AppleTalk Zone Information");
  public static final INetworkPort TCP_207_at_7 = _registerPort (207, ENetworkProtocol.TCP, "at-7", "AppleTalk Unused");
  public static final INetworkPort UDP_207_at_7 = _registerPort (207, ENetworkProtocol.UDP, "at-7", "AppleTalk Unused");
  public static final INetworkPort UDP_208_at_8 = _registerPort (208, ENetworkProtocol.UDP, "at-8", "AppleTalk Unused");
  public static final INetworkPort TCP_208_at_8 = _registerPort (208, ENetworkProtocol.TCP, "at-8", "AppleTalk Unused");
  public static final INetworkPort UDP_209_qmtp = _registerPort (209,
                                                                 ENetworkProtocol.UDP,
                                                                 "qmtp",
                                                                 "The Quick Mail Transfer Protocol");
  public static final INetworkPort TCP_209_qmtp = _registerPort (209,
                                                                 ENetworkProtocol.TCP,
                                                                 "qmtp",
                                                                 "The Quick Mail Transfer Protocol");
  public static final INetworkPort TCP_210_z39_50 = _registerPort (210, ENetworkProtocol.TCP, "z39.50", "ANSI Z39.50");
  public static final INetworkPort UDP_210_z39_50 = _registerPort (210, ENetworkProtocol.UDP, "z39.50", "ANSI Z39.50");
  public static final INetworkPort TCP_211_914c_g = _registerPort (211,
                                                                   ENetworkProtocol.TCP,
                                                                   "914c/g",
                                                                   "Texas Instruments 914C/G Terminal");
  public static final INetworkPort UDP_211_914c_g = _registerPort (211,
                                                                   ENetworkProtocol.UDP,
                                                                   "914c/g",
                                                                   "Texas Instruments 914C/G Terminal");
  public static final INetworkPort UDP_212_anet = _registerPort (212, ENetworkProtocol.UDP, "anet", "ATEXSSTR");
  public static final INetworkPort TCP_212_anet = _registerPort (212, ENetworkProtocol.TCP, "anet", "ATEXSSTR");
  public static final INetworkPort TCP_213_ipx = _registerPort (213, ENetworkProtocol.TCP, "ipx", "IPX");
  public static final INetworkPort UDP_213_ipx = _registerPort (213, ENetworkProtocol.UDP, "ipx", "IPX");
  public static final INetworkPort TCP_214_vmpwscs = _registerPort (214, ENetworkProtocol.TCP, "vmpwscs", "VM PWSCS");
  public static final INetworkPort UDP_214_vmpwscs = _registerPort (214, ENetworkProtocol.UDP, "vmpwscs", "VM PWSCS");
  public static final INetworkPort TCP_215_softpc = _registerPort (215,
                                                                   ENetworkProtocol.TCP,
                                                                   "softpc",
                                                                   "Insignia Solutions");
  public static final INetworkPort UDP_215_softpc = _registerPort (215,
                                                                   ENetworkProtocol.UDP,
                                                                   "softpc",
                                                                   "Insignia Solutions");
  public static final INetworkPort UDP_216_CAIlic = _registerPort (216,
                                                                   ENetworkProtocol.UDP,
                                                                   "CAIlic",
                                                                   "Computer Associates Int'l License Server");
  public static final INetworkPort TCP_216_CAIlic = _registerPort (216,
                                                                   ENetworkProtocol.TCP,
                                                                   "CAIlic",
                                                                   "Computer Associates Int'l License Server");
  public static final INetworkPort UDP_217_dbase = _registerPort (217, ENetworkProtocol.UDP, "dbase", "dBASE Unix");
  public static final INetworkPort TCP_217_dbase = _registerPort (217, ENetworkProtocol.TCP, "dbase", "dBASE Unix");
  public static final INetworkPort TCP_218_mpp = _registerPort (218,
                                                                ENetworkProtocol.TCP,
                                                                "mpp",
                                                                "Netix Message Posting Protocol");
  public static final INetworkPort UDP_218_mpp = _registerPort (218,
                                                                ENetworkProtocol.UDP,
                                                                "mpp",
                                                                "Netix Message Posting Protocol");
  public static final INetworkPort UDP_219_uarps = _registerPort (219, ENetworkProtocol.UDP, "uarps", "Unisys ARPs");
  public static final INetworkPort TCP_219_uarps = _registerPort (219, ENetworkProtocol.TCP, "uarps", "Unisys ARPs");
  public static final INetworkPort TCP_220_imap3 = _registerPort (220,
                                                                  ENetworkProtocol.TCP,
                                                                  "imap3",
                                                                  "Interactive Mail Access Protocol v3");
  public static final INetworkPort UDP_220_imap3 = _registerPort (220,
                                                                  ENetworkProtocol.UDP,
                                                                  "imap3",
                                                                  "Interactive Mail Access Protocol v3");
  public static final INetworkPort TCP_221_fln_spx = _registerPort (221,
                                                                    ENetworkProtocol.TCP,
                                                                    "fln-spx",
                                                                    "Berkeley rlogind with SPX auth");
  public static final INetworkPort UDP_221_fln_spx = _registerPort (221,
                                                                    ENetworkProtocol.UDP,
                                                                    "fln-spx",
                                                                    "Berkeley rlogind with SPX auth");
  public static final INetworkPort TCP_222_rsh_spx = _registerPort (222,
                                                                    ENetworkProtocol.TCP,
                                                                    "rsh-spx",
                                                                    "Berkeley rshd with SPX auth");
  public static final INetworkPort UDP_222_rsh_spx = _registerPort (222,
                                                                    ENetworkProtocol.UDP,
                                                                    "rsh-spx",
                                                                    "Berkeley rshd with SPX auth");
  public static final INetworkPort TCP_223_cdc = _registerPort (223,
                                                                ENetworkProtocol.TCP,
                                                                "cdc",
                                                                "Certificate Distribution Center");
  public static final INetworkPort UDP_223_cdc = _registerPort (223,
                                                                ENetworkProtocol.UDP,
                                                                "cdc",
                                                                "Certificate Distribution Center");
  public static final INetworkPort TCP_224_masqdialer = _registerPort (224,
                                                                       ENetworkProtocol.TCP,
                                                                       "masqdialer",
                                                                       "masqdialer");
  public static final INetworkPort UDP_224_masqdialer = _registerPort (224,
                                                                       ENetworkProtocol.UDP,
                                                                       "masqdialer",
                                                                       "masqdialer");
  public static final INetworkPort TCP_242_direct = _registerPort (242, ENetworkProtocol.TCP, "direct", "Direct");
  public static final INetworkPort UDP_242_direct = _registerPort (242, ENetworkProtocol.UDP, "direct", "Direct");
  public static final INetworkPort TCP_243_sur_meas = _registerPort (243,
                                                                     ENetworkProtocol.TCP,
                                                                     "sur-meas",
                                                                     "Survey Measurement");
  public static final INetworkPort UDP_243_sur_meas = _registerPort (243,
                                                                     ENetworkProtocol.UDP,
                                                                     "sur-meas",
                                                                     "Survey Measurement");
  public static final INetworkPort UDP_244_inbusiness = _registerPort (244,
                                                                       ENetworkProtocol.UDP,
                                                                       "inbusiness",
                                                                       "inbusiness");
  public static final INetworkPort TCP_244_inbusiness = _registerPort (244,
                                                                       ENetworkProtocol.TCP,
                                                                       "inbusiness",
                                                                       "inbusiness");
  public static final INetworkPort TCP_245_link = _registerPort (245, ENetworkProtocol.TCP, "link", "LINK");
  public static final INetworkPort UDP_245_link = _registerPort (245, ENetworkProtocol.UDP, "link", "LINK");
  public static final INetworkPort TCP_246_dsp3270 = _registerPort (246,
                                                                    ENetworkProtocol.TCP,
                                                                    "dsp3270",
                                                                    "Display Systems Protocol");
  public static final INetworkPort UDP_246_dsp3270 = _registerPort (246,
                                                                    ENetworkProtocol.UDP,
                                                                    "dsp3270",
                                                                    "Display Systems Protocol");
  public static final INetworkPort UDP_247_subntbcst_tftp = _registerPort (247,
                                                                           ENetworkProtocol.UDP,
                                                                           "subntbcst_tftp",
                                                                           "SUBNTBCST_TFTP");
  public static final INetworkPort TCP_247_subntbcst_tftp = _registerPort (247,
                                                                           ENetworkProtocol.TCP,
                                                                           "subntbcst_tftp",
                                                                           "SUBNTBCST_TFTP");
  public static final INetworkPort TCP_248_bhfhs = _registerPort (248, ENetworkProtocol.TCP, "bhfhs", "bhfhs");
  public static final INetworkPort UDP_248_bhfhs = _registerPort (248, ENetworkProtocol.UDP, "bhfhs", "bhfhs");
  public static final INetworkPort TCP_256_rap = _registerPort (256, ENetworkProtocol.TCP, "rap", "RAP");
  public static final INetworkPort UDP_256_rap = _registerPort (256, ENetworkProtocol.UDP, "rap", "RAP");
  public static final INetworkPort TCP_257_set = _registerPort (257,
                                                                ENetworkProtocol.TCP,
                                                                "set",
                                                                "Secure Electronic Transaction");
  public static final INetworkPort UDP_257_set = _registerPort (257,
                                                                ENetworkProtocol.UDP,
                                                                "set",
                                                                "Secure Electronic Transaction");
  public static final INetworkPort TCP_259_esro_gen = _registerPort (259,
                                                                     ENetworkProtocol.TCP,
                                                                     "esro-gen",
                                                                     "Efficient Short Remote Operations");
  public static final INetworkPort UDP_259_esro_gen = _registerPort (259,
                                                                     ENetworkProtocol.UDP,
                                                                     "esro-gen",
                                                                     "Efficient Short Remote Operations");
  public static final INetworkPort TCP_260_openport = _registerPort (260, ENetworkProtocol.TCP, "openport", "Openport");
  public static final INetworkPort UDP_260_openport = _registerPort (260, ENetworkProtocol.UDP, "openport", "Openport");
  public static final INetworkPort TCP_261_nsiiops = _registerPort (261,
                                                                    ENetworkProtocol.TCP,
                                                                    "nsiiops",
                                                                    "IIOP Name Service over TLS/SSL");
  public static final INetworkPort UDP_261_nsiiops = _registerPort (261,
                                                                    ENetworkProtocol.UDP,
                                                                    "nsiiops",
                                                                    "IIOP Name Service over TLS/SSL");
  public static final INetworkPort UDP_262_arcisdms = _registerPort (262, ENetworkProtocol.UDP, "arcisdms", "Arcisdms");
  public static final INetworkPort TCP_262_arcisdms = _registerPort (262, ENetworkProtocol.TCP, "arcisdms", "Arcisdms");
  public static final INetworkPort TCP_263_hdap = _registerPort (263, ENetworkProtocol.TCP, "hdap", "HDAP");
  public static final INetworkPort UDP_263_hdap = _registerPort (263, ENetworkProtocol.UDP, "hdap", "HDAP");
  public static final INetworkPort UDP_264_bgmp = _registerPort (264, ENetworkProtocol.UDP, "bgmp", "BGMP");
  public static final INetworkPort TCP_264_bgmp = _registerPort (264, ENetworkProtocol.TCP, "bgmp", "BGMP");
  public static final INetworkPort TCP_265_x_bone_ctl = _registerPort (265,
                                                                       ENetworkProtocol.TCP,
                                                                       "x-bone-ctl",
                                                                       "X-Bone CTL");
  public static final INetworkPort UDP_265_x_bone_ctl = _registerPort (265,
                                                                       ENetworkProtocol.UDP,
                                                                       "x-bone-ctl",
                                                                       "X-Bone CTL");
  public static final INetworkPort TCP_266_sst = _registerPort (266, ENetworkProtocol.TCP, "sst", "SCSI on ST");
  public static final INetworkPort UDP_266_sst = _registerPort (266, ENetworkProtocol.UDP, "sst", "SCSI on ST");
  public static final INetworkPort TCP_267_td_service = _registerPort (267,
                                                                       ENetworkProtocol.TCP,
                                                                       "td-service",
                                                                       "Tobit David Service Layer");
  public static final INetworkPort UDP_267_td_service = _registerPort (267,
                                                                       ENetworkProtocol.UDP,
                                                                       "td-service",
                                                                       "Tobit David Service Layer");
  public static final INetworkPort UDP_268_td_replica = _registerPort (268,
                                                                       ENetworkProtocol.UDP,
                                                                       "td-replica",
                                                                       "Tobit David Replica");
  public static final INetworkPort TCP_268_td_replica = _registerPort (268,
                                                                       ENetworkProtocol.TCP,
                                                                       "td-replica",
                                                                       "Tobit David Replica");
  public static final INetworkPort UDP_269_manet = _registerPort (269, ENetworkProtocol.UDP, "manet", "MANET Protocols");
  public static final INetworkPort TCP_269_manet = _registerPort (269, ENetworkProtocol.TCP, "manet", "MANET Protocols");
  public static final INetworkPort UDP_280_http_mgmt = _registerPort (280,
                                                                      ENetworkProtocol.UDP,
                                                                      "http-mgmt",
                                                                      "http-mgmt");
  public static final INetworkPort TCP_280_http_mgmt = _registerPort (280,
                                                                      ENetworkProtocol.TCP,
                                                                      "http-mgmt",
                                                                      "http-mgmt");
  public static final INetworkPort UDP_281_personal_link = _registerPort (281,
                                                                          ENetworkProtocol.UDP,
                                                                          "personal-link",
                                                                          "Personal Link");
  public static final INetworkPort TCP_281_personal_link = _registerPort (281,
                                                                          ENetworkProtocol.TCP,
                                                                          "personal-link",
                                                                          "Personal Link");
  public static final INetworkPort TCP_282_cableport_ax = _registerPort (282,
                                                                         ENetworkProtocol.TCP,
                                                                         "cableport-ax",
                                                                         "Cable Port A/X");
  public static final INetworkPort UDP_282_cableport_ax = _registerPort (282,
                                                                         ENetworkProtocol.UDP,
                                                                         "cableport-ax",
                                                                         "Cable Port A/X");
  public static final INetworkPort TCP_283_rescap = _registerPort (283, ENetworkProtocol.TCP, "rescap", "rescap");
  public static final INetworkPort UDP_283_rescap = _registerPort (283, ENetworkProtocol.UDP, "rescap", "rescap");
  public static final INetworkPort UDP_284_corerjd = _registerPort (284, ENetworkProtocol.UDP, "corerjd", "corerjd");
  public static final INetworkPort TCP_284_corerjd = _registerPort (284, ENetworkProtocol.TCP, "corerjd", "corerjd");
  public static final INetworkPort TCP_286_fxp = _registerPort (286, ENetworkProtocol.TCP, "fxp", "FXP Communication");
  public static final INetworkPort UDP_286_fxp = _registerPort (286, ENetworkProtocol.UDP, "fxp", "FXP Communication");
  public static final INetworkPort UDP_287_k_block = _registerPort (287, ENetworkProtocol.UDP, "k-block", "K-BLOCK");
  public static final INetworkPort TCP_287_k_block = _registerPort (287, ENetworkProtocol.TCP, "k-block", "K-BLOCK");
  public static final INetworkPort UDP_308_novastorbakcup = _registerPort (308,
                                                                           ENetworkProtocol.UDP,
                                                                           "novastorbakcup",
                                                                           "Novastor Backup");
  public static final INetworkPort TCP_308_novastorbakcup = _registerPort (308,
                                                                           ENetworkProtocol.TCP,
                                                                           "novastorbakcup",
                                                                           "Novastor Backup");
  public static final INetworkPort TCP_309_entrusttime = _registerPort (309,
                                                                        ENetworkProtocol.TCP,
                                                                        "entrusttime",
                                                                        "EntrustTime");
  public static final INetworkPort UDP_309_entrusttime = _registerPort (309,
                                                                        ENetworkProtocol.UDP,
                                                                        "entrusttime",
                                                                        "EntrustTime");
  public static final INetworkPort TCP_310_bhmds = _registerPort (310, ENetworkProtocol.TCP, "bhmds", "bhmds");
  public static final INetworkPort UDP_310_bhmds = _registerPort (310, ENetworkProtocol.UDP, "bhmds", "bhmds");
  public static final INetworkPort UDP_311_asip_webadmin = _registerPort (311,
                                                                          ENetworkProtocol.UDP,
                                                                          "asip-webadmin",
                                                                          "AppleShare IP WebAdmin");
  public static final INetworkPort TCP_311_asip_webadmin = _registerPort (311,
                                                                          ENetworkProtocol.TCP,
                                                                          "asip-webadmin",
                                                                          "AppleShare IP WebAdmin");
  public static final INetworkPort UDP_312_vslmp = _registerPort (312, ENetworkProtocol.UDP, "vslmp", "VSLMP");
  public static final INetworkPort TCP_312_vslmp = _registerPort (312, ENetworkProtocol.TCP, "vslmp", "VSLMP");
  public static final INetworkPort TCP_313_magenta_logic = _registerPort (313,
                                                                          ENetworkProtocol.TCP,
                                                                          "magenta-logic",
                                                                          "Magenta Logic");
  public static final INetworkPort UDP_313_magenta_logic = _registerPort (313,
                                                                          ENetworkProtocol.UDP,
                                                                          "magenta-logic",
                                                                          "Magenta Logic");
  public static final INetworkPort TCP_314_opalis_robot = _registerPort (314,
                                                                         ENetworkProtocol.TCP,
                                                                         "opalis-robot",
                                                                         "Opalis Robot");
  public static final INetworkPort UDP_314_opalis_robot = _registerPort (314,
                                                                         ENetworkProtocol.UDP,
                                                                         "opalis-robot",
                                                                         "Opalis Robot");
  public static final INetworkPort UDP_315_dpsi = _registerPort (315, ENetworkProtocol.UDP, "dpsi", "DPSI");
  public static final INetworkPort TCP_315_dpsi = _registerPort (315, ENetworkProtocol.TCP, "dpsi", "DPSI");
  public static final INetworkPort TCP_316_decauth = _registerPort (316, ENetworkProtocol.TCP, "decauth", "decAuth");
  public static final INetworkPort UDP_316_decauth = _registerPort (316, ENetworkProtocol.UDP, "decauth", "decAuth");
  public static final INetworkPort UDP_317_zannet = _registerPort (317, ENetworkProtocol.UDP, "zannet", "Zannet");
  public static final INetworkPort TCP_317_zannet = _registerPort (317, ENetworkProtocol.TCP, "zannet", "Zannet");
  public static final INetworkPort UDP_318_pkix_timestamp = _registerPort (318,
                                                                           ENetworkProtocol.UDP,
                                                                           "pkix-timestamp",
                                                                           "PKIX TimeStamp");
  public static final INetworkPort TCP_318_pkix_timestamp = _registerPort (318,
                                                                           ENetworkProtocol.TCP,
                                                                           "pkix-timestamp",
                                                                           "PKIX TimeStamp");
  public static final INetworkPort UDP_319_ptp_event = _registerPort (319,
                                                                      ENetworkProtocol.UDP,
                                                                      "ptp-event",
                                                                      "PTP Event");
  public static final INetworkPort TCP_319_ptp_event = _registerPort (319,
                                                                      ENetworkProtocol.TCP,
                                                                      "ptp-event",
                                                                      "PTP Event");
  public static final INetworkPort TCP_320_ptp_general = _registerPort (320,
                                                                        ENetworkProtocol.TCP,
                                                                        "ptp-general",
                                                                        "PTP General");
  public static final INetworkPort UDP_320_ptp_general = _registerPort (320,
                                                                        ENetworkProtocol.UDP,
                                                                        "ptp-general",
                                                                        "PTP General");
  public static final INetworkPort UDP_321_pip = _registerPort (321, ENetworkProtocol.UDP, "pip", "PIP");
  public static final INetworkPort TCP_321_pip = _registerPort (321, ENetworkProtocol.TCP, "pip", "PIP");
  public static final INetworkPort TCP_322_rtsps = _registerPort (322, ENetworkProtocol.TCP, "rtsps", "RTSPS");
  public static final INetworkPort UDP_322_rtsps = _registerPort (322, ENetworkProtocol.UDP, "rtsps", "RTSPS");
  public static final INetworkPort TCP_333_texar = _registerPort (333,
                                                                  ENetworkProtocol.TCP,
                                                                  "texar",
                                                                  "Texar Security Port");
  public static final INetworkPort UDP_333_texar = _registerPort (333,
                                                                  ENetworkProtocol.UDP,
                                                                  "texar",
                                                                  "Texar Security Port");
  public static final INetworkPort UDP_344_pdap = _registerPort (344,
                                                                 ENetworkProtocol.UDP,
                                                                 "pdap",
                                                                 "Prospero Data Access Protocol");
  public static final INetworkPort TCP_344_pdap = _registerPort (344,
                                                                 ENetworkProtocol.TCP,
                                                                 "pdap",
                                                                 "Prospero Data Access Protocol");
  public static final INetworkPort TCP_345_pawserv = _registerPort (345,
                                                                    ENetworkProtocol.TCP,
                                                                    "pawserv",
                                                                    "Perf Analysis Workbench");
  public static final INetworkPort UDP_345_pawserv = _registerPort (345,
                                                                    ENetworkProtocol.UDP,
                                                                    "pawserv",
                                                                    "Perf Analysis Workbench");
  public static final INetworkPort UDP_346_zserv = _registerPort (346, ENetworkProtocol.UDP, "zserv", "Zebra server");
  public static final INetworkPort TCP_346_zserv = _registerPort (346, ENetworkProtocol.TCP, "zserv", "Zebra server");
  public static final INetworkPort UDP_347_fatserv = _registerPort (347,
                                                                    ENetworkProtocol.UDP,
                                                                    "fatserv",
                                                                    "Fatmen Server");
  public static final INetworkPort TCP_347_fatserv = _registerPort (347,
                                                                    ENetworkProtocol.TCP,
                                                                    "fatserv",
                                                                    "Fatmen Server");
  public static final INetworkPort TCP_348_csi_sgwp = _registerPort (348,
                                                                     ENetworkProtocol.TCP,
                                                                     "csi-sgwp",
                                                                     "Cabletron Management Protocol");
  public static final INetworkPort UDP_348_csi_sgwp = _registerPort (348,
                                                                     ENetworkProtocol.UDP,
                                                                     "csi-sgwp",
                                                                     "Cabletron Management Protocol");
  public static final INetworkPort TCP_349_mftp = _registerPort (349, ENetworkProtocol.TCP, "mftp", "mftp");
  public static final INetworkPort UDP_349_mftp = _registerPort (349, ENetworkProtocol.UDP, "mftp", "mftp");
  public static final INetworkPort UDP_350_matip_type_a = _registerPort (350,
                                                                         ENetworkProtocol.UDP,
                                                                         "matip-type-a",
                                                                         "MATIP Type A");
  public static final INetworkPort TCP_350_matip_type_a = _registerPort (350,
                                                                         ENetworkProtocol.TCP,
                                                                         "matip-type-a",
                                                                         "MATIP Type A");
  public static final INetworkPort UDP_351_bhoetty = _registerPort (351, ENetworkProtocol.UDP, "bhoetty", "bhoetty");
  public static final INetworkPort UDP_351_matip_type_b = _registerPort (351,
                                                                         ENetworkProtocol.UDP,
                                                                         "matip-type-b",
                                                                         "MATIP Type B");
  public static final INetworkPort TCP_351_bhoetty = _registerPort (351,
                                                                    ENetworkProtocol.TCP,
                                                                    "bhoetty",
                                                                    "bhoetty (added 5/21/97)");
  public static final INetworkPort TCP_351_matip_type_b = _registerPort (351,
                                                                         ENetworkProtocol.TCP,
                                                                         "matip-type-b",
                                                                         "MATIP Type B");
  public static final INetworkPort TCP_352_dtag_ste_sb = _registerPort (352,
                                                                        ENetworkProtocol.TCP,
                                                                        "dtag-ste-sb",
                                                                        "DTAG (assigned long ago)");
  public static final INetworkPort UDP_352_bhoedap4 = _registerPort (352, ENetworkProtocol.UDP, "bhoedap4", "bhoedap4");
  public static final INetworkPort TCP_352_bhoedap4 = _registerPort (352,
                                                                     ENetworkProtocol.TCP,
                                                                     "bhoedap4",
                                                                     "bhoedap4 (added 5/21/97)");
  public static final INetworkPort UDP_352_dtag_ste_sb = _registerPort (352,
                                                                        ENetworkProtocol.UDP,
                                                                        "dtag-ste-sb",
                                                                        "DTAG");
  public static final INetworkPort UDP_353_ndsauth = _registerPort (353, ENetworkProtocol.UDP, "ndsauth", "NDSAUTH");
  public static final INetworkPort TCP_353_ndsauth = _registerPort (353, ENetworkProtocol.TCP, "ndsauth", "NDSAUTH");
  public static final INetworkPort UDP_354_bh611 = _registerPort (354, ENetworkProtocol.UDP, "bh611", "bh611");
  public static final INetworkPort TCP_354_bh611 = _registerPort (354, ENetworkProtocol.TCP, "bh611", "bh611");
  public static final INetworkPort TCP_355_datex_asn = _registerPort (355,
                                                                      ENetworkProtocol.TCP,
                                                                      "datex-asn",
                                                                      "DATEX-ASN");
  public static final INetworkPort UDP_355_datex_asn = _registerPort (355,
                                                                      ENetworkProtocol.UDP,
                                                                      "datex-asn",
                                                                      "DATEX-ASN");
  public static final INetworkPort UDP_356_cloanto_net_1 = _registerPort (356,
                                                                          ENetworkProtocol.UDP,
                                                                          "cloanto-net-1",
                                                                          "Cloanto Net 1");
  public static final INetworkPort TCP_356_cloanto_net_1 = _registerPort (356,
                                                                          ENetworkProtocol.TCP,
                                                                          "cloanto-net-1",
                                                                          "Cloanto Net 1");
  public static final INetworkPort TCP_357_bhevent = _registerPort (357, ENetworkProtocol.TCP, "bhevent", "bhevent");
  public static final INetworkPort UDP_357_bhevent = _registerPort (357, ENetworkProtocol.UDP, "bhevent", "bhevent");
  public static final INetworkPort TCP_358_shrinkwrap = _registerPort (358,
                                                                       ENetworkProtocol.TCP,
                                                                       "shrinkwrap",
                                                                       "Shrinkwrap");
  public static final INetworkPort UDP_358_shrinkwrap = _registerPort (358,
                                                                       ENetworkProtocol.UDP,
                                                                       "shrinkwrap",
                                                                       "Shrinkwrap");
  public static final INetworkPort UDP_359_nsrmp = _registerPort (359,
                                                                  ENetworkProtocol.UDP,
                                                                  "nsrmp",
                                                                  "Network Security Risk Management Protocol");
  public static final INetworkPort TCP_359_nsrmp = _registerPort (359,
                                                                  ENetworkProtocol.TCP,
                                                                  "nsrmp",
                                                                  "Network Security Risk Management Protocol");
  public static final INetworkPort TCP_360_scoi2odialog = _registerPort (360,
                                                                         ENetworkProtocol.TCP,
                                                                         "scoi2odialog",
                                                                         "scoi2odialog");
  public static final INetworkPort UDP_360_scoi2odialog = _registerPort (360,
                                                                         ENetworkProtocol.UDP,
                                                                         "scoi2odialog",
                                                                         "scoi2odialog");
  public static final INetworkPort UDP_361_semantix = _registerPort (361, ENetworkProtocol.UDP, "semantix", "Semantix");
  public static final INetworkPort TCP_361_semantix = _registerPort (361, ENetworkProtocol.TCP, "semantix", "Semantix");
  public static final INetworkPort UDP_362_srssend = _registerPort (362, ENetworkProtocol.UDP, "srssend", "SRS Send");
  public static final INetworkPort TCP_362_srssend = _registerPort (362, ENetworkProtocol.TCP, "srssend", "SRS Send");
  public static final INetworkPort UDP_363_rsvp_tunnel = _registerPort (363,
                                                                        ENetworkProtocol.UDP,
                                                                        "rsvp_tunnel",
                                                                        "RSVP Tunnel");
  public static final INetworkPort TCP_363_rsvp_tunnel = _registerPort (363,
                                                                        ENetworkProtocol.TCP,
                                                                        "rsvp_tunnel",
                                                                        "RSVP Tunnel");
  public static final INetworkPort TCP_364_aurora_cmgr = _registerPort (364,
                                                                        ENetworkProtocol.TCP,
                                                                        "aurora-cmgr",
                                                                        "Aurora CMGR");
  public static final INetworkPort UDP_364_aurora_cmgr = _registerPort (364,
                                                                        ENetworkProtocol.UDP,
                                                                        "aurora-cmgr",
                                                                        "Aurora CMGR");
  public static final INetworkPort UDP_365_dtk = _registerPort (365, ENetworkProtocol.UDP, "dtk", "DTK");
  public static final INetworkPort TCP_365_dtk = _registerPort (365, ENetworkProtocol.TCP, "dtk", "DTK");
  public static final INetworkPort UDP_366_odmr = _registerPort (366, ENetworkProtocol.UDP, "odmr", "ODMR");
  public static final INetworkPort TCP_366_odmr = _registerPort (366, ENetworkProtocol.TCP, "odmr", "ODMR");
  public static final INetworkPort TCP_367_mortgageware = _registerPort (367,
                                                                         ENetworkProtocol.TCP,
                                                                         "mortgageware",
                                                                         "MortgageWare");
  public static final INetworkPort UDP_367_mortgageware = _registerPort (367,
                                                                         ENetworkProtocol.UDP,
                                                                         "mortgageware",
                                                                         "MortgageWare");
  public static final INetworkPort TCP_368_qbikgdp = _registerPort (368, ENetworkProtocol.TCP, "qbikgdp", "QbikGDP");
  public static final INetworkPort UDP_368_qbikgdp = _registerPort (368, ENetworkProtocol.UDP, "qbikgdp", "QbikGDP");
  public static final INetworkPort TCP_369_rpc2portmap = _registerPort (369,
                                                                        ENetworkProtocol.TCP,
                                                                        "rpc2portmap",
                                                                        "rpc2portmap");
  public static final INetworkPort UDP_369_rpc2portmap = _registerPort (369,
                                                                        ENetworkProtocol.UDP,
                                                                        "rpc2portmap",
                                                                        "rpc2portmap");
  public static final INetworkPort TCP_370_codaauth2 = _registerPort (370,
                                                                      ENetworkProtocol.TCP,
                                                                      "codaauth2",
                                                                      "codaauth2");
  public static final INetworkPort UDP_370_codaauth2 = _registerPort (370,
                                                                      ENetworkProtocol.UDP,
                                                                      "codaauth2",
                                                                      "codaauth2");
  public static final INetworkPort UDP_371_clearcase = _registerPort (371,
                                                                      ENetworkProtocol.UDP,
                                                                      "clearcase",
                                                                      "Clearcase");
  public static final INetworkPort TCP_371_clearcase = _registerPort (371,
                                                                      ENetworkProtocol.TCP,
                                                                      "clearcase",
                                                                      "Clearcase");
  public static final INetworkPort UDP_372_ulistproc = _registerPort (372,
                                                                      ENetworkProtocol.UDP,
                                                                      "ulistproc",
                                                                      "ListProcessor");
  public static final INetworkPort TCP_372_ulistproc = _registerPort (372,
                                                                      ENetworkProtocol.TCP,
                                                                      "ulistproc",
                                                                      "ListProcessor");
  public static final INetworkPort TCP_373_legent_1 = _registerPort (373,
                                                                     ENetworkProtocol.TCP,
                                                                     "legent-1",
                                                                     "Legent Corporation");
  public static final INetworkPort UDP_373_legent_1 = _registerPort (373,
                                                                     ENetworkProtocol.UDP,
                                                                     "legent-1",
                                                                     "Legent Corporation");
  public static final INetworkPort TCP_374_legent_2 = _registerPort (374,
                                                                     ENetworkProtocol.TCP,
                                                                     "legent-2",
                                                                     "Legent Corporation");
  public static final INetworkPort UDP_374_legent_2 = _registerPort (374,
                                                                     ENetworkProtocol.UDP,
                                                                     "legent-2",
                                                                     "Legent Corporation");
  public static final INetworkPort TCP_375_hassle = _registerPort (375, ENetworkProtocol.TCP, "hassle", "Hassle");
  public static final INetworkPort UDP_375_hassle = _registerPort (375, ENetworkProtocol.UDP, "hassle", "Hassle");
  public static final INetworkPort UDP_376_nip = _registerPort (376,
                                                                ENetworkProtocol.UDP,
                                                                "nip",
                                                                "Amiga Envoy Network Inquiry Proto");
  public static final INetworkPort TCP_376_nip = _registerPort (376,
                                                                ENetworkProtocol.TCP,
                                                                "nip",
                                                                "Amiga Envoy Network Inquiry Proto");
  public static final INetworkPort TCP_377_tnETOS = _registerPort (377,
                                                                   ENetworkProtocol.TCP,
                                                                   "tnETOS",
                                                                   "NEC Corporation");
  public static final INetworkPort UDP_377_tnETOS = _registerPort (377,
                                                                   ENetworkProtocol.UDP,
                                                                   "tnETOS",
                                                                   "NEC Corporation");
  public static final INetworkPort TCP_378_dsETOS = _registerPort (378,
                                                                   ENetworkProtocol.TCP,
                                                                   "dsETOS",
                                                                   "NEC Corporation");
  public static final INetworkPort UDP_378_dsETOS = _registerPort (378,
                                                                   ENetworkProtocol.UDP,
                                                                   "dsETOS",
                                                                   "NEC Corporation");
  public static final INetworkPort TCP_379_is99c = _registerPort (379,
                                                                  ENetworkProtocol.TCP,
                                                                  "is99c",
                                                                  "TIA/EIA/IS-99 modem client");
  public static final INetworkPort UDP_379_is99c = _registerPort (379,
                                                                  ENetworkProtocol.UDP,
                                                                  "is99c",
                                                                  "TIA/EIA/IS-99 modem client");
  public static final INetworkPort UDP_380_is99s = _registerPort (380,
                                                                  ENetworkProtocol.UDP,
                                                                  "is99s",
                                                                  "TIA/EIA/IS-99 modem server");
  public static final INetworkPort TCP_380_is99s = _registerPort (380,
                                                                  ENetworkProtocol.TCP,
                                                                  "is99s",
                                                                  "TIA/EIA/IS-99 modem server");
  public static final INetworkPort UDP_381_hp_collector = _registerPort (381,
                                                                         ENetworkProtocol.UDP,
                                                                         "hp-collector",
                                                                         "hp performance data collector");
  public static final INetworkPort TCP_381_hp_collector = _registerPort (381,
                                                                         ENetworkProtocol.TCP,
                                                                         "hp-collector",
                                                                         "hp performance data collector");
  public static final INetworkPort TCP_382_hp_managed_node = _registerPort (382,
                                                                            ENetworkProtocol.TCP,
                                                                            "hp-managed-node",
                                                                            "hp performance data managed node");
  public static final INetworkPort UDP_382_hp_managed_node = _registerPort (382,
                                                                            ENetworkProtocol.UDP,
                                                                            "hp-managed-node",
                                                                            "hp performance data managed node");
  public static final INetworkPort UDP_383_hp_alarm_mgr = _registerPort (383,
                                                                         ENetworkProtocol.UDP,
                                                                         "hp-alarm-mgr",
                                                                         "hp performance data alarm manager");
  public static final INetworkPort TCP_383_hp_alarm_mgr = _registerPort (383,
                                                                         ENetworkProtocol.TCP,
                                                                         "hp-alarm-mgr",
                                                                         "hp performance data alarm manager");
  public static final INetworkPort TCP_384_arns = _registerPort (384,
                                                                 ENetworkProtocol.TCP,
                                                                 "arns",
                                                                 "A Remote Network Server System");
  public static final INetworkPort UDP_384_arns = _registerPort (384,
                                                                 ENetworkProtocol.UDP,
                                                                 "arns",
                                                                 "A Remote Network Server System");
  public static final INetworkPort UDP_385_ibm_app = _registerPort (385,
                                                                    ENetworkProtocol.UDP,
                                                                    "ibm-app",
                                                                    "IBM Application");
  public static final INetworkPort TCP_385_ibm_app = _registerPort (385,
                                                                    ENetworkProtocol.TCP,
                                                                    "ibm-app",
                                                                    "IBM Application");
  public static final INetworkPort TCP_386_asa = _registerPort (386,
                                                                ENetworkProtocol.TCP,
                                                                "asa",
                                                                "ASA Message Router Object Def.");
  public static final INetworkPort UDP_386_asa = _registerPort (386,
                                                                ENetworkProtocol.UDP,
                                                                "asa",
                                                                "ASA Message Router Object Def.");
  public static final INetworkPort TCP_387_aurp = _registerPort (387,
                                                                 ENetworkProtocol.TCP,
                                                                 "aurp",
                                                                 "Appletalk Update-Based Routing Pro.");
  public static final INetworkPort UDP_387_aurp = _registerPort (387,
                                                                 ENetworkProtocol.UDP,
                                                                 "aurp",
                                                                 "Appletalk Update-Based Routing Pro.");
  public static final INetworkPort UDP_388_unidata_ldm = _registerPort (388,
                                                                        ENetworkProtocol.UDP,
                                                                        "unidata-ldm",
                                                                        "Unidata LDM");
  public static final INetworkPort TCP_388_unidata_ldm = _registerPort (388,
                                                                        ENetworkProtocol.TCP,
                                                                        "unidata-ldm",
                                                                        "Unidata LDM");
  public static final INetworkPort TCP_389_ldap = _registerPort (389,
                                                                 ENetworkProtocol.TCP,
                                                                 "ldap",
                                                                 "Lightweight Directory Access Protocol");
  public static final INetworkPort UDP_389_ldap = _registerPort (389,
                                                                 ENetworkProtocol.UDP,
                                                                 "ldap",
                                                                 "Lightweight Directory Access Protocol");
  public static final INetworkPort UDP_390_uis = _registerPort (390, ENetworkProtocol.UDP, "uis", "UIS");
  public static final INetworkPort TCP_390_uis = _registerPort (390, ENetworkProtocol.TCP, "uis", "UIS");
  public static final INetworkPort UDP_391_synotics_relay = _registerPort (391,
                                                                           ENetworkProtocol.UDP,
                                                                           "synotics-relay",
                                                                           "SynOptics SNMP Relay Port");
  public static final INetworkPort TCP_391_synotics_relay = _registerPort (391,
                                                                           ENetworkProtocol.TCP,
                                                                           "synotics-relay",
                                                                           "SynOptics SNMP Relay Port");
  public static final INetworkPort UDP_392_synotics_broker = _registerPort (392,
                                                                            ENetworkProtocol.UDP,
                                                                            "synotics-broker",
                                                                            "SynOptics Port Broker Port");
  public static final INetworkPort TCP_392_synotics_broker = _registerPort (392,
                                                                            ENetworkProtocol.TCP,
                                                                            "synotics-broker",
                                                                            "SynOptics Port Broker Port");
  public static final INetworkPort UDP_393_meta5 = _registerPort (393, ENetworkProtocol.UDP, "meta5", "Meta5");
  public static final INetworkPort TCP_393_meta5 = _registerPort (393, ENetworkProtocol.TCP, "meta5", "Meta5");
  public static final INetworkPort TCP_394_embl_ndt = _registerPort (394,
                                                                     ENetworkProtocol.TCP,
                                                                     "embl-ndt",
                                                                     "EMBL Nucleic Data Transfer");
  public static final INetworkPort UDP_394_embl_ndt = _registerPort (394,
                                                                     ENetworkProtocol.UDP,
                                                                     "embl-ndt",
                                                                     "EMBL Nucleic Data Transfer");
  public static final INetworkPort TCP_395_netcp = _registerPort (395,
                                                                  ENetworkProtocol.TCP,
                                                                  "netcp",
                                                                  "NETscout Control Protocol");
  public static final INetworkPort UDP_395_netcp = _registerPort (395,
                                                                  ENetworkProtocol.UDP,
                                                                  "netcp",
                                                                  "NETscout Control Protocol");
  public static final INetworkPort TCP_396_netware_ip = _registerPort (396,
                                                                       ENetworkProtocol.TCP,
                                                                       "netware-ip",
                                                                       "Novell Netware over IP");
  public static final INetworkPort UDP_396_netware_ip = _registerPort (396,
                                                                       ENetworkProtocol.UDP,
                                                                       "netware-ip",
                                                                       "Novell Netware over IP");
  public static final INetworkPort TCP_397_mptn = _registerPort (397,
                                                                 ENetworkProtocol.TCP,
                                                                 "mptn",
                                                                 "Multi Protocol Trans. Net.");
  public static final INetworkPort UDP_397_mptn = _registerPort (397,
                                                                 ENetworkProtocol.UDP,
                                                                 "mptn",
                                                                 "Multi Protocol Trans. Net.");
  public static final INetworkPort UDP_398_kryptolan = _registerPort (398,
                                                                      ENetworkProtocol.UDP,
                                                                      "kryptolan",
                                                                      "Kryptolan");
  public static final INetworkPort TCP_398_kryptolan = _registerPort (398,
                                                                      ENetworkProtocol.TCP,
                                                                      "kryptolan",
                                                                      "Kryptolan");
  public static final INetworkPort UDP_399_iso_tsap_c2 = _registerPort (399,
                                                                        ENetworkProtocol.UDP,
                                                                        "iso-tsap-c2",
                                                                        "ISO Transport Class 2 Non-Control over UDP");
  public static final INetworkPort TCP_399_iso_tsap_c2 = _registerPort (399,
                                                                        ENetworkProtocol.TCP,
                                                                        "iso-tsap-c2",
                                                                        "ISO Transport Class 2 Non-Control over TCP");
  public static final INetworkPort UDP_400_osb_sd = _registerPort (400,
                                                                   ENetworkProtocol.UDP,
                                                                   "osb-sd",
                                                                   "Oracle Secure Backup");
  public static final INetworkPort TCP_400_osb_sd = _registerPort (400,
                                                                   ENetworkProtocol.TCP,
                                                                   "osb-sd",
                                                                   "Oracle Secure Backup");
  public static final INetworkPort UDP_401_ups = _registerPort (401,
                                                                ENetworkProtocol.UDP,
                                                                "ups",
                                                                "Uninterruptible Power Supply");
  public static final INetworkPort TCP_401_ups = _registerPort (401,
                                                                ENetworkProtocol.TCP,
                                                                "ups",
                                                                "Uninterruptible Power Supply");
  public static final INetworkPort TCP_402_genie = _registerPort (402, ENetworkProtocol.TCP, "genie", "Genie Protocol");
  public static final INetworkPort UDP_402_genie = _registerPort (402, ENetworkProtocol.UDP, "genie", "Genie Protocol");
  public static final INetworkPort UDP_403_decap = _registerPort (403, ENetworkProtocol.UDP, "decap", "decap");
  public static final INetworkPort TCP_403_decap = _registerPort (403, ENetworkProtocol.TCP, "decap", "decap");
  public static final INetworkPort UDP_404_nced = _registerPort (404, ENetworkProtocol.UDP, "nced", "nced");
  public static final INetworkPort TCP_404_nced = _registerPort (404, ENetworkProtocol.TCP, "nced", "nced");
  public static final INetworkPort UDP_405_ncld = _registerPort (405, ENetworkProtocol.UDP, "ncld", "ncld");
  public static final INetworkPort TCP_405_ncld = _registerPort (405, ENetworkProtocol.TCP, "ncld", "ncld");
  public static final INetworkPort UDP_406_imsp = _registerPort (406,
                                                                 ENetworkProtocol.UDP,
                                                                 "imsp",
                                                                 "Interactive Mail Support Protocol");
  public static final INetworkPort TCP_406_imsp = _registerPort (406,
                                                                 ENetworkProtocol.TCP,
                                                                 "imsp",
                                                                 "Interactive Mail Support Protocol");
  public static final INetworkPort UDP_407_timbuktu = _registerPort (407, ENetworkProtocol.UDP, "timbuktu", "Timbuktu");
  public static final INetworkPort TCP_407_timbuktu = _registerPort (407, ENetworkProtocol.TCP, "timbuktu", "Timbuktu");
  public static final INetworkPort UDP_408_prm_sm = _registerPort (408,
                                                                   ENetworkProtocol.UDP,
                                                                   "prm-sm",
                                                                   "Prospero Resource Manager Sys. Man.");
  public static final INetworkPort TCP_408_prm_sm = _registerPort (408,
                                                                   ENetworkProtocol.TCP,
                                                                   "prm-sm",
                                                                   "Prospero Resource Manager Sys. Man.");
  public static final INetworkPort UDP_409_prm_nm = _registerPort (409,
                                                                   ENetworkProtocol.UDP,
                                                                   "prm-nm",
                                                                   "Prospero Resource Manager Node Man.");
  public static final INetworkPort TCP_409_prm_nm = _registerPort (409,
                                                                   ENetworkProtocol.TCP,
                                                                   "prm-nm",
                                                                   "Prospero Resource Manager Node Man.");
  public static final INetworkPort TCP_410_decladebug = _registerPort (410,
                                                                       ENetworkProtocol.TCP,
                                                                       "decladebug",
                                                                       "DECLadebug Remote Debug Protocol");
  public static final INetworkPort UDP_410_decladebug = _registerPort (410,
                                                                       ENetworkProtocol.UDP,
                                                                       "decladebug",
                                                                       "DECLadebug Remote Debug Protocol");
  public static final INetworkPort UDP_411_rmt = _registerPort (411, ENetworkProtocol.UDP, "rmt", "Remote MT Protocol");
  public static final INetworkPort TCP_411_rmt = _registerPort (411, ENetworkProtocol.TCP, "rmt", "Remote MT Protocol");
  public static final INetworkPort UDP_412_synoptics_trap = _registerPort (412,
                                                                           ENetworkProtocol.UDP,
                                                                           "synoptics-trap",
                                                                           "Trap Convention Port");
  public static final INetworkPort TCP_412_synoptics_trap = _registerPort (412,
                                                                           ENetworkProtocol.TCP,
                                                                           "synoptics-trap",
                                                                           "Trap Convention Port");
  public static final INetworkPort UDP_413_smsp = _registerPort (413,
                                                                 ENetworkProtocol.UDP,
                                                                 "smsp",
                                                                 "Storage Management Services Protocol");
  public static final INetworkPort TCP_413_smsp = _registerPort (413,
                                                                 ENetworkProtocol.TCP,
                                                                 "smsp",
                                                                 "Storage Management Services Protocol");
  public static final INetworkPort UDP_414_infoseek = _registerPort (414, ENetworkProtocol.UDP, "infoseek", "InfoSeek");
  public static final INetworkPort TCP_414_infoseek = _registerPort (414, ENetworkProtocol.TCP, "infoseek", "InfoSeek");
  public static final INetworkPort UDP_415_bnet = _registerPort (415, ENetworkProtocol.UDP, "bnet", "BNet");
  public static final INetworkPort TCP_415_bnet = _registerPort (415, ENetworkProtocol.TCP, "bnet", "BNet");
  public static final INetworkPort UDP_416_silverplatter = _registerPort (416,
                                                                          ENetworkProtocol.UDP,
                                                                          "silverplatter",
                                                                          "Silverplatter");
  public static final INetworkPort TCP_416_silverplatter = _registerPort (416,
                                                                          ENetworkProtocol.TCP,
                                                                          "silverplatter",
                                                                          "Silverplatter");
  public static final INetworkPort TCP_417_onmux = _registerPort (417, ENetworkProtocol.TCP, "onmux", "Onmux");
  public static final INetworkPort UDP_417_onmux = _registerPort (417, ENetworkProtocol.UDP, "onmux", "Onmux");
  public static final INetworkPort UDP_418_hyper_g = _registerPort (418, ENetworkProtocol.UDP, "hyper-g", "Hyper-G");
  public static final INetworkPort TCP_418_hyper_g = _registerPort (418, ENetworkProtocol.TCP, "hyper-g", "Hyper-G");
  public static final INetworkPort UDP_419_ariel1 = _registerPort (419, ENetworkProtocol.UDP, "ariel1", "Ariel 1");
  public static final INetworkPort TCP_419_ariel1 = _registerPort (419, ENetworkProtocol.TCP, "ariel1", "Ariel 1");
  public static final INetworkPort UDP_420_smpte = _registerPort (420, ENetworkProtocol.UDP, "smpte", "SMPTE");
  public static final INetworkPort TCP_420_smpte = _registerPort (420, ENetworkProtocol.TCP, "smpte", "SMPTE");
  public static final INetworkPort TCP_421_ariel2 = _registerPort (421, ENetworkProtocol.TCP, "ariel2", "Ariel 2");
  public static final INetworkPort UDP_421_ariel2 = _registerPort (421, ENetworkProtocol.UDP, "ariel2", "Ariel 2");
  public static final INetworkPort UDP_422_ariel3 = _registerPort (422, ENetworkProtocol.UDP, "ariel3", "Ariel 3");
  public static final INetworkPort TCP_422_ariel3 = _registerPort (422, ENetworkProtocol.TCP, "ariel3", "Ariel 3");
  public static final INetworkPort TCP_423_opc_job_start = _registerPort (423,
                                                                          ENetworkProtocol.TCP,
                                                                          "opc-job-start",
                                                                          "IBM Operations Planning and Control Start");
  public static final INetworkPort UDP_423_opc_job_start = _registerPort (423,
                                                                          ENetworkProtocol.UDP,
                                                                          "opc-job-start",
                                                                          "IBM Operations Planning and Control Start");
  public static final INetworkPort UDP_424_opc_job_track = _registerPort (424,
                                                                          ENetworkProtocol.UDP,
                                                                          "opc-job-track",
                                                                          "IBM Operations Planning and Control Track");
  public static final INetworkPort TCP_424_opc_job_track = _registerPort (424,
                                                                          ENetworkProtocol.TCP,
                                                                          "opc-job-track",
                                                                          "IBM Operations Planning and Control Track");
  public static final INetworkPort TCP_425_icad_el = _registerPort (425, ENetworkProtocol.TCP, "icad-el", "ICAD");
  public static final INetworkPort UDP_425_icad_el = _registerPort (425, ENetworkProtocol.UDP, "icad-el", "ICAD");
  public static final INetworkPort TCP_426_smartsdp = _registerPort (426, ENetworkProtocol.TCP, "smartsdp", "smartsdp");
  public static final INetworkPort UDP_426_smartsdp = _registerPort (426, ENetworkProtocol.UDP, "smartsdp", "smartsdp");
  public static final INetworkPort UDP_427_svrloc = _registerPort (427,
                                                                   ENetworkProtocol.UDP,
                                                                   "svrloc",
                                                                   "Server Location");
  public static final INetworkPort TCP_427_svrloc = _registerPort (427,
                                                                   ENetworkProtocol.TCP,
                                                                   "svrloc",
                                                                   "Server Location");
  public static final INetworkPort UDP_428_ocs_cmu = _registerPort (428, ENetworkProtocol.UDP, "ocs_cmu", "OCS_CMU");
  public static final INetworkPort TCP_428_ocs_cmu = _registerPort (428, ENetworkProtocol.TCP, "ocs_cmu", "OCS_CMU");
  public static final INetworkPort UDP_429_ocs_amu = _registerPort (429, ENetworkProtocol.UDP, "ocs_amu", "OCS_AMU");
  public static final INetworkPort TCP_429_ocs_amu = _registerPort (429, ENetworkProtocol.TCP, "ocs_amu", "OCS_AMU");
  public static final INetworkPort TCP_430_utmpsd = _registerPort (430, ENetworkProtocol.TCP, "utmpsd", "UTMPSD");
  public static final INetworkPort UDP_430_utmpsd = _registerPort (430, ENetworkProtocol.UDP, "utmpsd", "UTMPSD");
  public static final INetworkPort UDP_431_utmpcd = _registerPort (431, ENetworkProtocol.UDP, "utmpcd", "UTMPCD");
  public static final INetworkPort TCP_431_utmpcd = _registerPort (431, ENetworkProtocol.TCP, "utmpcd", "UTMPCD");
  public static final INetworkPort UDP_432_iasd = _registerPort (432, ENetworkProtocol.UDP, "iasd", "IASD");
  public static final INetworkPort TCP_432_iasd = _registerPort (432, ENetworkProtocol.TCP, "iasd", "IASD");
  public static final INetworkPort UDP_433_nnsp = _registerPort (433, ENetworkProtocol.UDP, "nnsp", "NNSP");
  public static final INetworkPort TCP_433_nnsp = _registerPort (433, ENetworkProtocol.TCP, "nnsp", "NNSP");
  public static final INetworkPort TCP_434_mobileip_agent = _registerPort (434,
                                                                           ENetworkProtocol.TCP,
                                                                           "mobileip-agent",
                                                                           "MobileIP-Agent");
  public static final INetworkPort UDP_434_mobileip_agent = _registerPort (434,
                                                                           ENetworkProtocol.UDP,
                                                                           "mobileip-agent",
                                                                           "MobileIP-Agent");
  public static final INetworkPort UDP_435_mobilip_mn = _registerPort (435,
                                                                       ENetworkProtocol.UDP,
                                                                       "mobilip-mn",
                                                                       "MobilIP-MN");
  public static final INetworkPort TCP_435_mobilip_mn = _registerPort (435,
                                                                       ENetworkProtocol.TCP,
                                                                       "mobilip-mn",
                                                                       "MobilIP-MN");
  public static final INetworkPort UDP_436_dna_cml = _registerPort (436, ENetworkProtocol.UDP, "dna-cml", "DNA-CML");
  public static final INetworkPort TCP_436_dna_cml = _registerPort (436, ENetworkProtocol.TCP, "dna-cml", "DNA-CML");
  public static final INetworkPort TCP_437_comscm = _registerPort (437, ENetworkProtocol.TCP, "comscm", "comscm");
  public static final INetworkPort UDP_437_comscm = _registerPort (437, ENetworkProtocol.UDP, "comscm", "comscm");
  public static final INetworkPort UDP_438_dsfgw = _registerPort (438, ENetworkProtocol.UDP, "dsfgw", "dsfgw");
  public static final INetworkPort TCP_438_dsfgw = _registerPort (438, ENetworkProtocol.TCP, "dsfgw", "dsfgw");
  public static final INetworkPort UDP_439_dasp = _registerPort (439,
                                                                 ENetworkProtocol.UDP,
                                                                 "dasp",
                                                                 "dasp      tommy&inlab.m.eunet.de");
  public static final INetworkPort TCP_439_dasp = _registerPort (439,
                                                                 ENetworkProtocol.TCP,
                                                                 "dasp",
                                                                 "dasp      Thomas Obermair");
  public static final INetworkPort UDP_440_sgcp = _registerPort (440, ENetworkProtocol.UDP, "sgcp", "sgcp");
  public static final INetworkPort TCP_440_sgcp = _registerPort (440, ENetworkProtocol.TCP, "sgcp", "sgcp");
  public static final INetworkPort UDP_441_decvms_sysmgt = _registerPort (441,
                                                                          ENetworkProtocol.UDP,
                                                                          "decvms-sysmgt",
                                                                          "decvms-sysmgt");
  public static final INetworkPort TCP_441_decvms_sysmgt = _registerPort (441,
                                                                          ENetworkProtocol.TCP,
                                                                          "decvms-sysmgt",
                                                                          "decvms-sysmgt");
  public static final INetworkPort UDP_442_cvc_hostd = _registerPort (442,
                                                                      ENetworkProtocol.UDP,
                                                                      "cvc_hostd",
                                                                      "cvc_hostd");
  public static final INetworkPort TCP_442_cvc_hostd = _registerPort (442,
                                                                      ENetworkProtocol.TCP,
                                                                      "cvc_hostd",
                                                                      "cvc_hostd");
  public static final INetworkPort UDP_443_https = _registerPort (443,
                                                                  ENetworkProtocol.UDP,
                                                                  "https",
                                                                  "http protocol over TLS/SSL");
  public static final INetworkPort TCP_443_https = _registerPort (443,
                                                                  ENetworkProtocol.TCP,
                                                                  "https",
                                                                  "http protocol over TLS/SSL");
  public static final INetworkPort TCP_444_snpp = _registerPort (444,
                                                                 ENetworkProtocol.TCP,
                                                                 "snpp",
                                                                 "Simple Network Paging Protocol");
  public static final INetworkPort UDP_444_snpp = _registerPort (444,
                                                                 ENetworkProtocol.UDP,
                                                                 "snpp",
                                                                 "Simple Network Paging Protocol");
  public static final INetworkPort TCP_445_microsoft_ds = _registerPort (445,
                                                                         ENetworkProtocol.TCP,
                                                                         "microsoft-ds",
                                                                         "Microsoft-DS");
  public static final INetworkPort UDP_445_microsoft_ds = _registerPort (445,
                                                                         ENetworkProtocol.UDP,
                                                                         "microsoft-ds",
                                                                         "Microsoft-DS");
  public static final INetworkPort TCP_446_ddm_rdb = _registerPort (446,
                                                                    ENetworkProtocol.TCP,
                                                                    "ddm-rdb",
                                                                    "DDM-Remote Relational Database Access");
  public static final INetworkPort UDP_446_ddm_rdb = _registerPort (446,
                                                                    ENetworkProtocol.UDP,
                                                                    "ddm-rdb",
                                                                    "DDM-Remote Relational Database Access");
  public static final INetworkPort UDP_447_ddm_dfm = _registerPort (447,
                                                                    ENetworkProtocol.UDP,
                                                                    "ddm-dfm",
                                                                    "DDM-Distributed File Management");
  public static final INetworkPort TCP_447_ddm_dfm = _registerPort (447,
                                                                    ENetworkProtocol.TCP,
                                                                    "ddm-dfm",
                                                                    "DDM-Distributed File Management");
  public static final INetworkPort TCP_448_ddm_ssl = _registerPort (448,
                                                                    ENetworkProtocol.TCP,
                                                                    "ddm-ssl",
                                                                    "DDM-Remote DB Access Using Secure Sockets");
  public static final INetworkPort UDP_448_ddm_ssl = _registerPort (448,
                                                                    ENetworkProtocol.UDP,
                                                                    "ddm-ssl",
                                                                    "DDM-Remote DB Access Using Secure Sockets");
  public static final INetworkPort TCP_449_as_servermap = _registerPort (449,
                                                                         ENetworkProtocol.TCP,
                                                                         "as-servermap",
                                                                         "AS Server Mapper");
  public static final INetworkPort UDP_449_as_servermap = _registerPort (449,
                                                                         ENetworkProtocol.UDP,
                                                                         "as-servermap",
                                                                         "AS Server Mapper");
  public static final INetworkPort UDP_450_tserver = _registerPort (450,
                                                                    ENetworkProtocol.UDP,
                                                                    "tserver",
                                                                    "Computer Supported Telecomunication Applications");
  public static final INetworkPort TCP_450_tserver = _registerPort (450,
                                                                    ENetworkProtocol.TCP,
                                                                    "tserver",
                                                                    "Computer Supported Telecomunication Applications");
  public static final INetworkPort TCP_451_sfs_smp_net = _registerPort (451,
                                                                        ENetworkProtocol.TCP,
                                                                        "sfs-smp-net",
                                                                        "Cray Network Semaphore server");
  public static final INetworkPort UDP_451_sfs_smp_net = _registerPort (451,
                                                                        ENetworkProtocol.UDP,
                                                                        "sfs-smp-net",
                                                                        "Cray Network Semaphore server");
  public static final INetworkPort TCP_452_sfs_config = _registerPort (452,
                                                                       ENetworkProtocol.TCP,
                                                                       "sfs-config",
                                                                       "Cray SFS config server");
  public static final INetworkPort UDP_452_sfs_config = _registerPort (452,
                                                                       ENetworkProtocol.UDP,
                                                                       "sfs-config",
                                                                       "Cray SFS config server");
  public static final INetworkPort TCP_453_creativeserver = _registerPort (453,
                                                                           ENetworkProtocol.TCP,
                                                                           "creativeserver",
                                                                           "CreativeServer");
  public static final INetworkPort UDP_453_creativeserver = _registerPort (453,
                                                                           ENetworkProtocol.UDP,
                                                                           "creativeserver",
                                                                           "CreativeServer");
  public static final INetworkPort UDP_454_contentserver = _registerPort (454,
                                                                          ENetworkProtocol.UDP,
                                                                          "contentserver",
                                                                          "ContentServer");
  public static final INetworkPort TCP_454_contentserver = _registerPort (454,
                                                                          ENetworkProtocol.TCP,
                                                                          "contentserver",
                                                                          "ContentServer");
  public static final INetworkPort TCP_455_creativepartnr = _registerPort (455,
                                                                           ENetworkProtocol.TCP,
                                                                           "creativepartnr",
                                                                           "CreativePartnr");
  public static final INetworkPort UDP_455_creativepartnr = _registerPort (455,
                                                                           ENetworkProtocol.UDP,
                                                                           "creativepartnr",
                                                                           "CreativePartnr");
  public static final INetworkPort UDP_456_macon_udp = _registerPort (456,
                                                                      ENetworkProtocol.UDP,
                                                                      "macon-udp",
                                                                      "macon-udp");
  public static final INetworkPort TCP_456_macon_tcp = _registerPort (456,
                                                                      ENetworkProtocol.TCP,
                                                                      "macon-tcp",
                                                                      "macon-tcp");
  public static final INetworkPort TCP_457_scohelp = _registerPort (457, ENetworkProtocol.TCP, "scohelp", "scohelp");
  public static final INetworkPort UDP_457_scohelp = _registerPort (457, ENetworkProtocol.UDP, "scohelp", "scohelp");
  public static final INetworkPort UDP_458_appleqtc = _registerPort (458,
                                                                     ENetworkProtocol.UDP,
                                                                     "appleqtc",
                                                                     "apple quick time");
  public static final INetworkPort TCP_458_appleqtc = _registerPort (458,
                                                                     ENetworkProtocol.TCP,
                                                                     "appleqtc",
                                                                     "apple quick time");
  public static final INetworkPort UDP_459_ampr_rcmd = _registerPort (459,
                                                                      ENetworkProtocol.UDP,
                                                                      "ampr-rcmd",
                                                                      "ampr-rcmd");
  public static final INetworkPort TCP_459_ampr_rcmd = _registerPort (459,
                                                                      ENetworkProtocol.TCP,
                                                                      "ampr-rcmd",
                                                                      "ampr-rcmd");
  public static final INetworkPort UDP_460_skronk = _registerPort (460, ENetworkProtocol.UDP, "skronk", "skronk");
  public static final INetworkPort TCP_460_skronk = _registerPort (460, ENetworkProtocol.TCP, "skronk", "skronk");
  public static final INetworkPort UDP_461_datasurfsrv = _registerPort (461,
                                                                        ENetworkProtocol.UDP,
                                                                        "datasurfsrv",
                                                                        "DataRampSrv");
  public static final INetworkPort TCP_461_datasurfsrv = _registerPort (461,
                                                                        ENetworkProtocol.TCP,
                                                                        "datasurfsrv",
                                                                        "DataRampSrv");
  public static final INetworkPort TCP_462_datasurfsrvsec = _registerPort (462,
                                                                           ENetworkProtocol.TCP,
                                                                           "datasurfsrvsec",
                                                                           "DataRampSrvSec");
  public static final INetworkPort UDP_462_datasurfsrvsec = _registerPort (462,
                                                                           ENetworkProtocol.UDP,
                                                                           "datasurfsrvsec",
                                                                           "DataRampSrvSec");
  public static final INetworkPort TCP_463_alpes = _registerPort (463, ENetworkProtocol.TCP, "alpes", "alpes");
  public static final INetworkPort UDP_463_alpes = _registerPort (463, ENetworkProtocol.UDP, "alpes", "alpes");
  public static final INetworkPort TCP_464_kpasswd = _registerPort (464, ENetworkProtocol.TCP, "kpasswd", "kpasswd");
  public static final INetworkPort UDP_464_kpasswd = _registerPort (464, ENetworkProtocol.UDP, "kpasswd", "kpasswd");
  public static final INetworkPort TCP_465_urd = _registerPort (465,
                                                                ENetworkProtocol.TCP,
                                                                "urd",
                                                                "URL Rendesvous Directory for SSM");
  public static final INetworkPort UDP_465_igmpv3lite = _registerPort (465,
                                                                       ENetworkProtocol.UDP,
                                                                       "igmpv3lite",
                                                                       "IGMP over UDP for SSM");
  public static final INetworkPort TCP_466_digital_vrc = _registerPort (466,
                                                                        ENetworkProtocol.TCP,
                                                                        "digital-vrc",
                                                                        "digital-vrc");
  public static final INetworkPort UDP_466_digital_vrc = _registerPort (466,
                                                                        ENetworkProtocol.UDP,
                                                                        "digital-vrc",
                                                                        "digital-vrc");
  public static final INetworkPort UDP_467_mylex_mapd = _registerPort (467,
                                                                       ENetworkProtocol.UDP,
                                                                       "mylex-mapd",
                                                                       "mylex-mapd");
  public static final INetworkPort TCP_467_mylex_mapd = _registerPort (467,
                                                                       ENetworkProtocol.TCP,
                                                                       "mylex-mapd",
                                                                       "mylex-mapd");
  public static final INetworkPort TCP_468_photuris = _registerPort (468, ENetworkProtocol.TCP, "photuris", "proturis");
  public static final INetworkPort UDP_468_photuris = _registerPort (468, ENetworkProtocol.UDP, "photuris", "proturis");
  public static final INetworkPort TCP_469_rcp = _registerPort (469,
                                                                ENetworkProtocol.TCP,
                                                                "rcp",
                                                                "Radio Control Protocol");
  public static final INetworkPort UDP_469_rcp = _registerPort (469,
                                                                ENetworkProtocol.UDP,
                                                                "rcp",
                                                                "Radio Control Protocol");
  public static final INetworkPort TCP_470_scx_proxy = _registerPort (470,
                                                                      ENetworkProtocol.TCP,
                                                                      "scx-proxy",
                                                                      "scx-proxy");
  public static final INetworkPort UDP_470_scx_proxy = _registerPort (470,
                                                                      ENetworkProtocol.UDP,
                                                                      "scx-proxy",
                                                                      "scx-proxy");
  public static final INetworkPort TCP_471_mondex = _registerPort (471, ENetworkProtocol.TCP, "mondex", "Mondex");
  public static final INetworkPort UDP_471_mondex = _registerPort (471, ENetworkProtocol.UDP, "mondex", "Mondex");
  public static final INetworkPort TCP_472_ljk_login = _registerPort (472,
                                                                      ENetworkProtocol.TCP,
                                                                      "ljk-login",
                                                                      "ljk-login");
  public static final INetworkPort UDP_472_ljk_login = _registerPort (472,
                                                                      ENetworkProtocol.UDP,
                                                                      "ljk-login",
                                                                      "ljk-login");
  public static final INetworkPort UDP_473_hybrid_pop = _registerPort (473,
                                                                       ENetworkProtocol.UDP,
                                                                       "hybrid-pop",
                                                                       "hybrid-pop");
  public static final INetworkPort TCP_473_hybrid_pop = _registerPort (473,
                                                                       ENetworkProtocol.TCP,
                                                                       "hybrid-pop",
                                                                       "hybrid-pop");
  public static final INetworkPort TCP_474_tn_tl_w1 = _registerPort (474, ENetworkProtocol.TCP, "tn-tl-w1", "tn-tl-w1");
  public static final INetworkPort UDP_474_tn_tl_w2 = _registerPort (474, ENetworkProtocol.UDP, "tn-tl-w2", "tn-tl-w2");
  public static final INetworkPort UDP_475_tcpnethaspsrv = _registerPort (475,
                                                                          ENetworkProtocol.UDP,
                                                                          "tcpnethaspsrv",
                                                                          "tcpnethaspsrv");
  public static final INetworkPort TCP_475_tcpnethaspsrv = _registerPort (475,
                                                                          ENetworkProtocol.TCP,
                                                                          "tcpnethaspsrv",
                                                                          "tcpnethaspsrv");
  public static final INetworkPort UDP_476_tn_tl_fd1 = _registerPort (476,
                                                                      ENetworkProtocol.UDP,
                                                                      "tn-tl-fd1",
                                                                      "tn-tl-fd1");
  public static final INetworkPort TCP_476_tn_tl_fd1 = _registerPort (476,
                                                                      ENetworkProtocol.TCP,
                                                                      "tn-tl-fd1",
                                                                      "tn-tl-fd1");
  public static final INetworkPort UDP_477_ss7ns = _registerPort (477, ENetworkProtocol.UDP, "ss7ns", "ss7ns");
  public static final INetworkPort TCP_477_ss7ns = _registerPort (477, ENetworkProtocol.TCP, "ss7ns", "ss7ns");
  public static final INetworkPort TCP_478_spsc = _registerPort (478, ENetworkProtocol.TCP, "spsc", "spsc");
  public static final INetworkPort UDP_478_spsc = _registerPort (478, ENetworkProtocol.UDP, "spsc", "spsc");
  public static final INetworkPort TCP_479_iafserver = _registerPort (479,
                                                                      ENetworkProtocol.TCP,
                                                                      "iafserver",
                                                                      "iafserver");
  public static final INetworkPort UDP_479_iafserver = _registerPort (479,
                                                                      ENetworkProtocol.UDP,
                                                                      "iafserver",
                                                                      "iafserver");
  public static final INetworkPort TCP_480_iafdbase = _registerPort (480, ENetworkProtocol.TCP, "iafdbase", "iafdbase");
  public static final INetworkPort UDP_480_iafdbase = _registerPort (480, ENetworkProtocol.UDP, "iafdbase", "iafdbase");
  public static final INetworkPort UDP_481_ph = _registerPort (481, ENetworkProtocol.UDP, "ph", "Ph service");
  public static final INetworkPort TCP_481_ph = _registerPort (481, ENetworkProtocol.TCP, "ph", "Ph service");
  public static final INetworkPort TCP_482_bgs_nsi = _registerPort (482, ENetworkProtocol.TCP, "bgs-nsi", "bgs-nsi");
  public static final INetworkPort UDP_482_bgs_nsi = _registerPort (482, ENetworkProtocol.UDP, "bgs-nsi", "bgs-nsi");
  public static final INetworkPort TCP_483_ulpnet = _registerPort (483, ENetworkProtocol.TCP, "ulpnet", "ulpnet");
  public static final INetworkPort UDP_483_ulpnet = _registerPort (483, ENetworkProtocol.UDP, "ulpnet", "ulpnet");
  public static final INetworkPort UDP_484_integra_sme = _registerPort (484,
                                                                        ENetworkProtocol.UDP,
                                                                        "integra-sme",
                                                                        "Integra Software Management Environment");
  public static final INetworkPort TCP_484_integra_sme = _registerPort (484,
                                                                        ENetworkProtocol.TCP,
                                                                        "integra-sme",
                                                                        "Integra Software Management Environment");
  public static final INetworkPort UDP_485_powerburst = _registerPort (485,
                                                                       ENetworkProtocol.UDP,
                                                                       "powerburst",
                                                                       "Air Soft Power Burst");
  public static final INetworkPort TCP_485_powerburst = _registerPort (485,
                                                                       ENetworkProtocol.TCP,
                                                                       "powerburst",
                                                                       "Air Soft Power Burst");
  public static final INetworkPort TCP_486_avian = _registerPort (486, ENetworkProtocol.TCP, "avian", "avian");
  public static final INetworkPort UDP_486_avian = _registerPort (486, ENetworkProtocol.UDP, "avian", "avian");
  public static final INetworkPort UDP_487_saft = _registerPort (487,
                                                                 ENetworkProtocol.UDP,
                                                                 "saft",
                                                                 "saft Simple Asynchronous File Transfer");
  public static final INetworkPort TCP_487_saft = _registerPort (487,
                                                                 ENetworkProtocol.TCP,
                                                                 "saft",
                                                                 "saft Simple Asynchronous File Transfer");
  public static final INetworkPort UDP_488_gss_http = _registerPort (488, ENetworkProtocol.UDP, "gss-http", "gss-http");
  public static final INetworkPort TCP_488_gss_http = _registerPort (488, ENetworkProtocol.TCP, "gss-http", "gss-http");
  public static final INetworkPort TCP_489_nest_protocol = _registerPort (489,
                                                                          ENetworkProtocol.TCP,
                                                                          "nest-protocol",
                                                                          "nest-protocol");
  public static final INetworkPort UDP_489_nest_protocol = _registerPort (489,
                                                                          ENetworkProtocol.UDP,
                                                                          "nest-protocol",
                                                                          "nest-protocol");
  public static final INetworkPort TCP_490_micom_pfs = _registerPort (490,
                                                                      ENetworkProtocol.TCP,
                                                                      "micom-pfs",
                                                                      "micom-pfs");
  public static final INetworkPort UDP_490_micom_pfs = _registerPort (490,
                                                                      ENetworkProtocol.UDP,
                                                                      "micom-pfs",
                                                                      "micom-pfs");
  public static final INetworkPort TCP_491_go_login = _registerPort (491, ENetworkProtocol.TCP, "go-login", "go-login");
  public static final INetworkPort UDP_491_go_login = _registerPort (491, ENetworkProtocol.UDP, "go-login", "go-login");
  public static final INetworkPort UDP_492_ticf_1 = _registerPort (492,
                                                                   ENetworkProtocol.UDP,
                                                                   "ticf-1",
                                                                   "Transport Independent Convergence for FNA");
  public static final INetworkPort TCP_492_ticf_1 = _registerPort (492,
                                                                   ENetworkProtocol.TCP,
                                                                   "ticf-1",
                                                                   "Transport Independent Convergence for FNA");
  public static final INetworkPort TCP_493_ticf_2 = _registerPort (493,
                                                                   ENetworkProtocol.TCP,
                                                                   "ticf-2",
                                                                   "Transport Independent Convergence for FNA");
  public static final INetworkPort UDP_493_ticf_2 = _registerPort (493,
                                                                   ENetworkProtocol.UDP,
                                                                   "ticf-2",
                                                                   "Transport Independent Convergence for FNA");
  public static final INetworkPort UDP_494_pov_ray = _registerPort (494, ENetworkProtocol.UDP, "pov-ray", "POV-Ray");
  public static final INetworkPort TCP_494_pov_ray = _registerPort (494, ENetworkProtocol.TCP, "pov-ray", "POV-Ray");
  public static final INetworkPort TCP_495_intecourier = _registerPort (495,
                                                                        ENetworkProtocol.TCP,
                                                                        "intecourier",
                                                                        "intecourier");
  public static final INetworkPort UDP_495_intecourier = _registerPort (495,
                                                                        ENetworkProtocol.UDP,
                                                                        "intecourier",
                                                                        "intecourier");
  public static final INetworkPort TCP_496_pim_rp_disc = _registerPort (496,
                                                                        ENetworkProtocol.TCP,
                                                                        "pim-rp-disc",
                                                                        "PIM-RP-DISC");
  public static final INetworkPort UDP_496_pim_rp_disc = _registerPort (496,
                                                                        ENetworkProtocol.UDP,
                                                                        "pim-rp-disc",
                                                                        "PIM-RP-DISC");
  public static final INetworkPort UDP_497_dantz = _registerPort (497, ENetworkProtocol.UDP, "dantz", "dantz");
  public static final INetworkPort TCP_497_dantz = _registerPort (497, ENetworkProtocol.TCP, "dantz", "dantz");
  public static final INetworkPort TCP_498_siam = _registerPort (498, ENetworkProtocol.TCP, "siam", "siam");
  public static final INetworkPort UDP_498_siam = _registerPort (498, ENetworkProtocol.UDP, "siam", "siam");
  public static final INetworkPort TCP_499_iso_ill = _registerPort (499,
                                                                    ENetworkProtocol.TCP,
                                                                    "iso-ill",
                                                                    "ISO ILL Protocol");
  public static final INetworkPort UDP_499_iso_ill = _registerPort (499,
                                                                    ENetworkProtocol.UDP,
                                                                    "iso-ill",
                                                                    "ISO ILL Protocol");
  public static final INetworkPort UDP_500_isakmp = _registerPort (500, ENetworkProtocol.UDP, "isakmp", "isakmp");
  public static final INetworkPort TCP_500_isakmp = _registerPort (500, ENetworkProtocol.TCP, "isakmp", "isakmp");
  public static final INetworkPort UDP_501_stmf = _registerPort (501, ENetworkProtocol.UDP, "stmf", "STMF");
  public static final INetworkPort TCP_501_stmf = _registerPort (501, ENetworkProtocol.TCP, "stmf", "STMF");
  public static final INetworkPort UDP_502_asa_appl_proto = _registerPort (502,
                                                                           ENetworkProtocol.UDP,
                                                                           "asa-appl-proto",
                                                                           "asa-appl-proto");
  public static final INetworkPort TCP_502_asa_appl_proto = _registerPort (502,
                                                                           ENetworkProtocol.TCP,
                                                                           "asa-appl-proto",
                                                                           "asa-appl-proto");
  public static final INetworkPort TCP_503_intrinsa = _registerPort (503, ENetworkProtocol.TCP, "intrinsa", "Intrinsa");
  public static final INetworkPort UDP_503_intrinsa = _registerPort (503, ENetworkProtocol.UDP, "intrinsa", "Intrinsa");
  public static final INetworkPort TCP_504_citadel = _registerPort (504, ENetworkProtocol.TCP, "citadel", "citadel");
  public static final INetworkPort UDP_504_citadel = _registerPort (504, ENetworkProtocol.UDP, "citadel", "citadel");
  public static final INetworkPort UDP_505_mailbox_lm = _registerPort (505,
                                                                       ENetworkProtocol.UDP,
                                                                       "mailbox-lm",
                                                                       "mailbox-lm");
  public static final INetworkPort TCP_505_mailbox_lm = _registerPort (505,
                                                                       ENetworkProtocol.TCP,
                                                                       "mailbox-lm",
                                                                       "mailbox-lm");
  public static final INetworkPort TCP_506_ohimsrv = _registerPort (506, ENetworkProtocol.TCP, "ohimsrv", "ohimsrv");
  public static final INetworkPort UDP_506_ohimsrv = _registerPort (506, ENetworkProtocol.UDP, "ohimsrv", "ohimsrv");
  public static final INetworkPort TCP_507_crs = _registerPort (507, ENetworkProtocol.TCP, "crs", "crs");
  public static final INetworkPort UDP_507_crs = _registerPort (507, ENetworkProtocol.UDP, "crs", "crs");
  public static final INetworkPort UDP_508_xvttp = _registerPort (508, ENetworkProtocol.UDP, "xvttp", "xvttp");
  public static final INetworkPort TCP_508_xvttp = _registerPort (508, ENetworkProtocol.TCP, "xvttp", "xvttp");
  public static final INetworkPort UDP_509_snare = _registerPort (509, ENetworkProtocol.UDP, "snare", "snare");
  public static final INetworkPort TCP_509_snare = _registerPort (509, ENetworkProtocol.TCP, "snare", "snare");
  public static final INetworkPort UDP_510_fcp = _registerPort (510, ENetworkProtocol.UDP, "fcp", "FirstClass Protocol");
  public static final INetworkPort TCP_510_fcp = _registerPort (510, ENetworkProtocol.TCP, "fcp", "FirstClass Protocol");
  public static final INetworkPort TCP_511_passgo = _registerPort (511, ENetworkProtocol.TCP, "passgo", "PassGo");
  public static final INetworkPort UDP_511_passgo = _registerPort (511, ENetworkProtocol.UDP, "passgo", "PassGo");
  public static final INetworkPort UDP_512_comsat = _registerPort (512, ENetworkProtocol.UDP, "comsat", "");
  public static final INetworkPort UDP_512_biff = _registerPort (512,
                                                                 ENetworkProtocol.UDP,
                                                                 "biff",
                                                                 "used by mail system to notify users");
  public static final INetworkPort TCP_512_exec = _registerPort (512,
                                                                 ENetworkProtocol.TCP,
                                                                 "exec",
                                                                 "remote process execution;");
  public static final INetworkPort TCP_513_login = _registerPort (513,
                                                                  ENetworkProtocol.TCP,
                                                                  "login",
                                                                  "remote login a la telnet;");
  public static final INetworkPort UDP_513_who = _registerPort (513,
                                                                ENetworkProtocol.UDP,
                                                                "who",
                                                                "maintains data bases showing who's");
  public static final INetworkPort UDP_514_syslog = _registerPort (514, ENetworkProtocol.UDP, "syslog", "");
  public static final INetworkPort TCP_514_shell = _registerPort (514, ENetworkProtocol.TCP, "shell", "cmd");
  public static final INetworkPort TCP_515_printer = _registerPort (515, ENetworkProtocol.TCP, "printer", "spooler");
  public static final INetworkPort UDP_515_printer = _registerPort (515, ENetworkProtocol.UDP, "printer", "spooler");
  public static final INetworkPort UDP_516_videotex = _registerPort (516, ENetworkProtocol.UDP, "videotex", "videotex");
  public static final INetworkPort TCP_516_videotex = _registerPort (516, ENetworkProtocol.TCP, "videotex", "videotex");
  public static final INetworkPort UDP_517_talk = _registerPort (517,
                                                                 ENetworkProtocol.UDP,
                                                                 "talk",
                                                                 "like tenex link, but across");
  public static final INetworkPort TCP_517_talk = _registerPort (517,
                                                                 ENetworkProtocol.TCP,
                                                                 "talk",
                                                                 "like tenex link, but across");
  public static final INetworkPort TCP_518_ntalk = _registerPort (518, ENetworkProtocol.TCP, "ntalk", "");
  public static final INetworkPort UDP_518_ntalk = _registerPort (518, ENetworkProtocol.UDP, "ntalk", "");
  public static final INetworkPort UDP_519_utime = _registerPort (519, ENetworkProtocol.UDP, "utime", "unixtime");
  public static final INetworkPort TCP_519_utime = _registerPort (519, ENetworkProtocol.TCP, "utime", "unixtime");
  public static final INetworkPort TCP_520_efs = _registerPort (520,
                                                                ENetworkProtocol.TCP,
                                                                "efs",
                                                                "extended file name server");
  public static final INetworkPort UDP_520_router = _registerPort (520,
                                                                   ENetworkProtocol.UDP,
                                                                   "router",
                                                                   "local routing process (on site);");
  public static final INetworkPort UDP_521_ripng = _registerPort (521, ENetworkProtocol.UDP, "ripng", "ripng");
  public static final INetworkPort TCP_521_ripng = _registerPort (521, ENetworkProtocol.TCP, "ripng", "ripng");
  public static final INetworkPort TCP_522_ulp = _registerPort (522, ENetworkProtocol.TCP, "ulp", "ULP");
  public static final INetworkPort UDP_522_ulp = _registerPort (522, ENetworkProtocol.UDP, "ulp", "ULP");
  public static final INetworkPort TCP_523_ibm_db2 = _registerPort (523, ENetworkProtocol.TCP, "ibm-db2", "IBM-DB2");
  public static final INetworkPort UDP_523_ibm_db2 = _registerPort (523, ENetworkProtocol.UDP, "ibm-db2", "IBM-DB2");
  public static final INetworkPort TCP_524_ncp = _registerPort (524, ENetworkProtocol.TCP, "ncp", "NCP");
  public static final INetworkPort UDP_524_ncp = _registerPort (524, ENetworkProtocol.UDP, "ncp", "NCP");
  public static final INetworkPort TCP_525_timed = _registerPort (525, ENetworkProtocol.TCP, "timed", "timeserver");
  public static final INetworkPort UDP_525_timed = _registerPort (525, ENetworkProtocol.UDP, "timed", "timeserver");
  public static final INetworkPort TCP_526_tempo = _registerPort (526, ENetworkProtocol.TCP, "tempo", "newdate");
  public static final INetworkPort UDP_526_tempo = _registerPort (526, ENetworkProtocol.UDP, "tempo", "newdate");
  public static final INetworkPort UDP_527_stx = _registerPort (527, ENetworkProtocol.UDP, "stx", "Stock IXChange");
  public static final INetworkPort TCP_527_stx = _registerPort (527, ENetworkProtocol.TCP, "stx", "Stock IXChange");
  public static final INetworkPort UDP_528_custix = _registerPort (528,
                                                                   ENetworkProtocol.UDP,
                                                                   "custix",
                                                                   "Customer IXChange");
  public static final INetworkPort TCP_528_custix = _registerPort (528,
                                                                   ENetworkProtocol.TCP,
                                                                   "custix",
                                                                   "Customer IXChange");
  public static final INetworkPort UDP_529_irc_serv = _registerPort (529, ENetworkProtocol.UDP, "irc-serv", "IRC-SERV");
  public static final INetworkPort TCP_529_irc_serv = _registerPort (529, ENetworkProtocol.TCP, "irc-serv", "IRC-SERV");
  public static final INetworkPort TCP_530_courier = _registerPort (530, ENetworkProtocol.TCP, "courier", "rpc");
  public static final INetworkPort UDP_530_courier = _registerPort (530, ENetworkProtocol.UDP, "courier", "rpc");
  public static final INetworkPort TCP_531_conference = _registerPort (531, ENetworkProtocol.TCP, "conference", "chat");
  public static final INetworkPort UDP_531_conference = _registerPort (531, ENetworkProtocol.UDP, "conference", "chat");
  public static final INetworkPort TCP_532_netnews = _registerPort (532, ENetworkProtocol.TCP, "netnews", "readnews");
  public static final INetworkPort UDP_532_netnews = _registerPort (532, ENetworkProtocol.UDP, "netnews", "readnews");
  public static final INetworkPort TCP_533_netwall = _registerPort (533,
                                                                    ENetworkProtocol.TCP,
                                                                    "netwall",
                                                                    "for emergency broadcasts");
  public static final INetworkPort UDP_533_netwall = _registerPort (533,
                                                                    ENetworkProtocol.UDP,
                                                                    "netwall",
                                                                    "for emergency broadcasts");
  public static final INetworkPort UDP_534_windream = _registerPort (534,
                                                                     ENetworkProtocol.UDP,
                                                                     "windream",
                                                                     "windream Admin");
  public static final INetworkPort TCP_534_windream = _registerPort (534,
                                                                     ENetworkProtocol.TCP,
                                                                     "windream",
                                                                     "windream Admin");
  public static final INetworkPort UDP_535_iiop = _registerPort (535, ENetworkProtocol.UDP, "iiop", "iiop");
  public static final INetworkPort TCP_535_iiop = _registerPort (535, ENetworkProtocol.TCP, "iiop", "iiop");
  public static final INetworkPort UDP_536_opalis_rdv = _registerPort (536,
                                                                       ENetworkProtocol.UDP,
                                                                       "opalis-rdv",
                                                                       "opalis-rdv");
  public static final INetworkPort TCP_536_opalis_rdv = _registerPort (536,
                                                                       ENetworkProtocol.TCP,
                                                                       "opalis-rdv",
                                                                       "opalis-rdv");
  public static final INetworkPort TCP_537_nmsp = _registerPort (537,
                                                                 ENetworkProtocol.TCP,
                                                                 "nmsp",
                                                                 "Networked Media Streaming Protocol");
  public static final INetworkPort UDP_537_nmsp = _registerPort (537,
                                                                 ENetworkProtocol.UDP,
                                                                 "nmsp",
                                                                 "Networked Media Streaming Protocol");
  public static final INetworkPort UDP_538_gdomap = _registerPort (538, ENetworkProtocol.UDP, "gdomap", "gdomap");
  public static final INetworkPort TCP_538_gdomap = _registerPort (538, ENetworkProtocol.TCP, "gdomap", "gdomap");
  public static final INetworkPort TCP_539_apertus_ldp = _registerPort (539,
                                                                        ENetworkProtocol.TCP,
                                                                        "apertus-ldp",
                                                                        "Apertus Technologies Load Determination");
  public static final INetworkPort UDP_539_apertus_ldp = _registerPort (539,
                                                                        ENetworkProtocol.UDP,
                                                                        "apertus-ldp",
                                                                        "Apertus Technologies Load Determination");
  public static final INetworkPort UDP_540_uucp = _registerPort (540, ENetworkProtocol.UDP, "uucp", "uucpd");
  public static final INetworkPort TCP_540_uucp = _registerPort (540, ENetworkProtocol.TCP, "uucp", "uucpd");
  public static final INetworkPort TCP_541_uucp_rlogin = _registerPort (541,
                                                                        ENetworkProtocol.TCP,
                                                                        "uucp-rlogin",
                                                                        "uucp-rlogin");
  public static final INetworkPort UDP_541_uucp_rlogin = _registerPort (541,
                                                                        ENetworkProtocol.UDP,
                                                                        "uucp-rlogin",
                                                                        "uucp-rlogin");
  public static final INetworkPort UDP_542_commerce = _registerPort (542, ENetworkProtocol.UDP, "commerce", "commerce");
  public static final INetworkPort TCP_542_commerce = _registerPort (542, ENetworkProtocol.TCP, "commerce", "commerce");
  public static final INetworkPort TCP_543_klogin = _registerPort (543, ENetworkProtocol.TCP, "klogin", "");
  public static final INetworkPort UDP_543_klogin = _registerPort (543, ENetworkProtocol.UDP, "klogin", "");
  public static final INetworkPort TCP_544_kshell = _registerPort (544, ENetworkProtocol.TCP, "kshell", "krcmd");
  public static final INetworkPort UDP_544_kshell = _registerPort (544, ENetworkProtocol.UDP, "kshell", "krcmd");
  public static final INetworkPort UDP_545_appleqtcsrvr = _registerPort (545,
                                                                         ENetworkProtocol.UDP,
                                                                         "appleqtcsrvr",
                                                                         "appleqtcsrvr");
  public static final INetworkPort TCP_545_appleqtcsrvr = _registerPort (545,
                                                                         ENetworkProtocol.TCP,
                                                                         "appleqtcsrvr",
                                                                         "appleqtcsrvr");
  public static final INetworkPort TCP_546_dhcpv6_client = _registerPort (546,
                                                                          ENetworkProtocol.TCP,
                                                                          "dhcpv6-client",
                                                                          "DHCPv6 Client");
  public static final INetworkPort UDP_546_dhcpv6_client = _registerPort (546,
                                                                          ENetworkProtocol.UDP,
                                                                          "dhcpv6-client",
                                                                          "DHCPv6 Client");
  public static final INetworkPort TCP_547_dhcpv6_server = _registerPort (547,
                                                                          ENetworkProtocol.TCP,
                                                                          "dhcpv6-server",
                                                                          "DHCPv6 Server");
  public static final INetworkPort UDP_547_dhcpv6_server = _registerPort (547,
                                                                          ENetworkProtocol.UDP,
                                                                          "dhcpv6-server",
                                                                          "DHCPv6 Server");
  public static final INetworkPort TCP_548_afpovertcp = _registerPort (548,
                                                                       ENetworkProtocol.TCP,
                                                                       "afpovertcp",
                                                                       "AFP over TCP");
  public static final INetworkPort UDP_548_afpovertcp = _registerPort (548,
                                                                       ENetworkProtocol.UDP,
                                                                       "afpovertcp",
                                                                       "AFP over TCP");
  public static final INetworkPort TCP_549_idfp = _registerPort (549, ENetworkProtocol.TCP, "idfp", "IDFP");
  public static final INetworkPort UDP_549_idfp = _registerPort (549, ENetworkProtocol.UDP, "idfp", "IDFP");
  public static final INetworkPort UDP_550_new_rwho = _registerPort (550, ENetworkProtocol.UDP, "new-rwho", "new-who");
  public static final INetworkPort TCP_550_new_rwho = _registerPort (550, ENetworkProtocol.TCP, "new-rwho", "new-who");
  public static final INetworkPort UDP_551_cybercash = _registerPort (551,
                                                                      ENetworkProtocol.UDP,
                                                                      "cybercash",
                                                                      "cybercash");
  public static final INetworkPort TCP_551_cybercash = _registerPort (551,
                                                                      ENetworkProtocol.TCP,
                                                                      "cybercash",
                                                                      "cybercash");
  public static final INetworkPort TCP_552_devshr_nts = _registerPort (552,
                                                                       ENetworkProtocol.TCP,
                                                                       "devshr-nts",
                                                                       "DeviceShare");
  public static final INetworkPort UDP_552_devshr_nts = _registerPort (552,
                                                                       ENetworkProtocol.UDP,
                                                                       "devshr-nts",
                                                                       "DeviceShare");
  public static final INetworkPort TCP_553_pirp = _registerPort (553, ENetworkProtocol.TCP, "pirp", "pirp");
  public static final INetworkPort UDP_553_pirp = _registerPort (553, ENetworkProtocol.UDP, "pirp", "pirp");
  public static final INetworkPort TCP_554_rtsp = _registerPort (554,
                                                                 ENetworkProtocol.TCP,
                                                                 "rtsp",
                                                                 "Real Time Streaming Protocol (RTSP)");
  public static final INetworkPort UDP_554_rtsp = _registerPort (554,
                                                                 ENetworkProtocol.UDP,
                                                                 "rtsp",
                                                                 "Real Time Streaming Protocol (RTSP)");
  public static final INetworkPort TCP_555_dsf = _registerPort (555, ENetworkProtocol.TCP, "dsf", "");
  public static final INetworkPort UDP_555_dsf = _registerPort (555, ENetworkProtocol.UDP, "dsf", "");
  public static final INetworkPort TCP_556_remotefs = _registerPort (556,
                                                                     ENetworkProtocol.TCP,
                                                                     "remotefs",
                                                                     "rfs server");
  public static final INetworkPort UDP_556_remotefs = _registerPort (556,
                                                                     ENetworkProtocol.UDP,
                                                                     "remotefs",
                                                                     "rfs server");
  public static final INetworkPort TCP_557_openvms_sysipc = _registerPort (557,
                                                                           ENetworkProtocol.TCP,
                                                                           "openvms-sysipc",
                                                                           "openvms-sysipc");
  public static final INetworkPort UDP_557_openvms_sysipc = _registerPort (557,
                                                                           ENetworkProtocol.UDP,
                                                                           "openvms-sysipc",
                                                                           "openvms-sysipc");
  public static final INetworkPort TCP_558_sdnskmp = _registerPort (558, ENetworkProtocol.TCP, "sdnskmp", "SDNSKMP");
  public static final INetworkPort UDP_558_sdnskmp = _registerPort (558, ENetworkProtocol.UDP, "sdnskmp", "SDNSKMP");
  public static final INetworkPort UDP_559_teedtap = _registerPort (559, ENetworkProtocol.UDP, "teedtap", "TEEDTAP");
  public static final INetworkPort TCP_559_teedtap = _registerPort (559, ENetworkProtocol.TCP, "teedtap", "TEEDTAP");
  public static final INetworkPort TCP_560_rmonitor = _registerPort (560, ENetworkProtocol.TCP, "rmonitor", "rmonitord");
  public static final INetworkPort UDP_560_rmonitor = _registerPort (560, ENetworkProtocol.UDP, "rmonitor", "rmonitord");
  public static final INetworkPort TCP_561_monitor = _registerPort (561, ENetworkProtocol.TCP, "monitor", "");
  public static final INetworkPort UDP_561_monitor = _registerPort (561, ENetworkProtocol.UDP, "monitor", "");
  public static final INetworkPort UDP_562_chshell = _registerPort (562, ENetworkProtocol.UDP, "chshell", "chcmd");
  public static final INetworkPort TCP_562_chshell = _registerPort (562, ENetworkProtocol.TCP, "chshell", "chcmd");
  public static final INetworkPort TCP_563_nntps = _registerPort (563,
                                                                  ENetworkProtocol.TCP,
                                                                  "nntps",
                                                                  "nntp protocol over TLS/SSL (was snntp)");
  public static final INetworkPort UDP_563_nntps = _registerPort (563,
                                                                  ENetworkProtocol.UDP,
                                                                  "nntps",
                                                                  "nntp protocol over TLS/SSL (was snntp)");
  public static final INetworkPort TCP_564_9pfs = _registerPort (564,
                                                                 ENetworkProtocol.TCP,
                                                                 "9pfs",
                                                                 "plan 9 file service");
  public static final INetworkPort UDP_564_9pfs = _registerPort (564,
                                                                 ENetworkProtocol.UDP,
                                                                 "9pfs",
                                                                 "plan 9 file service");
  public static final INetworkPort UDP_565_whoami = _registerPort (565, ENetworkProtocol.UDP, "whoami", "whoami");
  public static final INetworkPort TCP_565_whoami = _registerPort (565, ENetworkProtocol.TCP, "whoami", "whoami");
  public static final INetworkPort UDP_566_streettalk = _registerPort (566,
                                                                       ENetworkProtocol.UDP,
                                                                       "streettalk",
                                                                       "streettalk");
  public static final INetworkPort TCP_566_streettalk = _registerPort (566,
                                                                       ENetworkProtocol.TCP,
                                                                       "streettalk",
                                                                       "streettalk");
  public static final INetworkPort UDP_567_banyan_rpc = _registerPort (567,
                                                                       ENetworkProtocol.UDP,
                                                                       "banyan-rpc",
                                                                       "banyan-rpc");
  public static final INetworkPort TCP_567_banyan_rpc = _registerPort (567,
                                                                       ENetworkProtocol.TCP,
                                                                       "banyan-rpc",
                                                                       "banyan-rpc");
  public static final INetworkPort TCP_568_ms_shuttle = _registerPort (568,
                                                                       ENetworkProtocol.TCP,
                                                                       "ms-shuttle",
                                                                       "microsoft shuttle");
  public static final INetworkPort UDP_568_ms_shuttle = _registerPort (568,
                                                                       ENetworkProtocol.UDP,
                                                                       "ms-shuttle",
                                                                       "microsoft shuttle");
  public static final INetworkPort TCP_569_ms_rome = _registerPort (569,
                                                                    ENetworkProtocol.TCP,
                                                                    "ms-rome",
                                                                    "microsoft rome");
  public static final INetworkPort UDP_569_ms_rome = _registerPort (569,
                                                                    ENetworkProtocol.UDP,
                                                                    "ms-rome",
                                                                    "microsoft rome");
  public static final INetworkPort TCP_570_meter = _registerPort (570, ENetworkProtocol.TCP, "meter", "demon");
  public static final INetworkPort UDP_570_meter = _registerPort (570, ENetworkProtocol.UDP, "meter", "demon");
  public static final INetworkPort UDP_571_meter = _registerPort (571, ENetworkProtocol.UDP, "meter", "udemon");
  public static final INetworkPort TCP_571_meter = _registerPort (571, ENetworkProtocol.TCP, "meter", "udemon");
  public static final INetworkPort TCP_572_sonar = _registerPort (572, ENetworkProtocol.TCP, "sonar", "sonar");
  public static final INetworkPort UDP_572_sonar = _registerPort (572, ENetworkProtocol.UDP, "sonar", "sonar");
  public static final INetworkPort UDP_573_banyan_vip = _registerPort (573,
                                                                       ENetworkProtocol.UDP,
                                                                       "banyan-vip",
                                                                       "banyan-vip");
  public static final INetworkPort TCP_573_banyan_vip = _registerPort (573,
                                                                       ENetworkProtocol.TCP,
                                                                       "banyan-vip",
                                                                       "banyan-vip");
  public static final INetworkPort UDP_574_ftp_agent = _registerPort (574,
                                                                      ENetworkProtocol.UDP,
                                                                      "ftp-agent",
                                                                      "FTP Software Agent System");
  public static final INetworkPort TCP_574_ftp_agent = _registerPort (574,
                                                                      ENetworkProtocol.TCP,
                                                                      "ftp-agent",
                                                                      "FTP Software Agent System");
  public static final INetworkPort UDP_575_vemmi = _registerPort (575, ENetworkProtocol.UDP, "vemmi", "VEMMI");
  public static final INetworkPort TCP_575_vemmi = _registerPort (575, ENetworkProtocol.TCP, "vemmi", "VEMMI");
  public static final INetworkPort TCP_576_ipcd = _registerPort (576, ENetworkProtocol.TCP, "ipcd", "ipcd");
  public static final INetworkPort UDP_576_ipcd = _registerPort (576, ENetworkProtocol.UDP, "ipcd", "ipcd");
  public static final INetworkPort UDP_577_vnas = _registerPort (577, ENetworkProtocol.UDP, "vnas", "vnas");
  public static final INetworkPort TCP_577_vnas = _registerPort (577, ENetworkProtocol.TCP, "vnas", "vnas");
  public static final INetworkPort TCP_578_ipdd = _registerPort (578, ENetworkProtocol.TCP, "ipdd", "ipdd");
  public static final INetworkPort UDP_578_ipdd = _registerPort (578, ENetworkProtocol.UDP, "ipdd", "ipdd");
  public static final INetworkPort TCP_579_decbsrv = _registerPort (579, ENetworkProtocol.TCP, "decbsrv", "decbsrv");
  public static final INetworkPort UDP_579_decbsrv = _registerPort (579, ENetworkProtocol.UDP, "decbsrv", "decbsrv");
  public static final INetworkPort TCP_580_sntp_heartbeat = _registerPort (580,
                                                                           ENetworkProtocol.TCP,
                                                                           "sntp-heartbeat",
                                                                           "SNTP HEARTBEAT");
  public static final INetworkPort UDP_580_sntp_heartbeat = _registerPort (580,
                                                                           ENetworkProtocol.UDP,
                                                                           "sntp-heartbeat",
                                                                           "SNTP HEARTBEAT");
  public static final INetworkPort UDP_581_bdp = _registerPort (581,
                                                                ENetworkProtocol.UDP,
                                                                "bdp",
                                                                "Bundle Discovery Protocol");
  public static final INetworkPort TCP_581_bdp = _registerPort (581,
                                                                ENetworkProtocol.TCP,
                                                                "bdp",
                                                                "Bundle Discovery Protocol");
  public static final INetworkPort UDP_582_scc_security = _registerPort (582,
                                                                         ENetworkProtocol.UDP,
                                                                         "scc-security",
                                                                         "SCC Security");
  public static final INetworkPort TCP_582_scc_security = _registerPort (582,
                                                                         ENetworkProtocol.TCP,
                                                                         "scc-security",
                                                                         "SCC Security");
  public static final INetworkPort UDP_583_philips_vc = _registerPort (583,
                                                                       ENetworkProtocol.UDP,
                                                                       "philips-vc",
                                                                       "Philips Video-Conferencing");
  public static final INetworkPort TCP_583_philips_vc = _registerPort (583,
                                                                       ENetworkProtocol.TCP,
                                                                       "philips-vc",
                                                                       "Philips Video-Conferencing");
  public static final INetworkPort TCP_584_keyserver = _registerPort (584,
                                                                      ENetworkProtocol.TCP,
                                                                      "keyserver",
                                                                      "Key Server");
  public static final INetworkPort UDP_584_keyserver = _registerPort (584,
                                                                      ENetworkProtocol.UDP,
                                                                      "keyserver",
                                                                      "Key Server");
  public static final INetworkPort TCP_586_password_chg = _registerPort (586,
                                                                         ENetworkProtocol.TCP,
                                                                         "password-chg",
                                                                         "Password Change");
  public static final INetworkPort UDP_586_password_chg = _registerPort (586,
                                                                         ENetworkProtocol.UDP,
                                                                         "password-chg",
                                                                         "Password Change");
  public static final INetworkPort TCP_587_submission = _registerPort (587,
                                                                       ENetworkProtocol.TCP,
                                                                       "submission",
                                                                       "Submission");
  public static final INetworkPort UDP_587_submission = _registerPort (587,
                                                                       ENetworkProtocol.UDP,
                                                                       "submission",
                                                                       "Submission");
  public static final INetworkPort TCP_588_cal = _registerPort (588, ENetworkProtocol.TCP, "cal", "CAL");
  public static final INetworkPort UDP_588_cal = _registerPort (588, ENetworkProtocol.UDP, "cal", "CAL");
  public static final INetworkPort UDP_589_eyelink = _registerPort (589, ENetworkProtocol.UDP, "eyelink", "EyeLink");
  public static final INetworkPort TCP_589_eyelink = _registerPort (589, ENetworkProtocol.TCP, "eyelink", "EyeLink");
  public static final INetworkPort UDP_590_tns_cml = _registerPort (590, ENetworkProtocol.UDP, "tns-cml", "TNS CML");
  public static final INetworkPort TCP_590_tns_cml = _registerPort (590, ENetworkProtocol.TCP, "tns-cml", "TNS CML");
  public static final INetworkPort UDP_591_http_alt = _registerPort (591,
                                                                     ENetworkProtocol.UDP,
                                                                     "http-alt",
                                                                     "FileMaker, Inc. - HTTP Alternate (see Port 80)");
  public static final INetworkPort TCP_591_http_alt = _registerPort (591,
                                                                     ENetworkProtocol.TCP,
                                                                     "http-alt",
                                                                     "FileMaker, Inc. - HTTP Alternate (see Port 80)");
  public static final INetworkPort UDP_592_eudora_set = _registerPort (592,
                                                                       ENetworkProtocol.UDP,
                                                                       "eudora-set",
                                                                       "Eudora Set");
  public static final INetworkPort TCP_592_eudora_set = _registerPort (592,
                                                                       ENetworkProtocol.TCP,
                                                                       "eudora-set",
                                                                       "Eudora Set");
  public static final INetworkPort TCP_593_http_rpc_epmap = _registerPort (593,
                                                                           ENetworkProtocol.TCP,
                                                                           "http-rpc-epmap",
                                                                           "HTTP RPC Ep Map");
  public static final INetworkPort UDP_593_http_rpc_epmap = _registerPort (593,
                                                                           ENetworkProtocol.UDP,
                                                                           "http-rpc-epmap",
                                                                           "HTTP RPC Ep Map");
  public static final INetworkPort UDP_594_tpip = _registerPort (594, ENetworkProtocol.UDP, "tpip", "TPIP");
  public static final INetworkPort TCP_594_tpip = _registerPort (594, ENetworkProtocol.TCP, "tpip", "TPIP");
  public static final INetworkPort TCP_595_cab_protocol = _registerPort (595,
                                                                         ENetworkProtocol.TCP,
                                                                         "cab-protocol",
                                                                         "CAB Protocol");
  public static final INetworkPort UDP_595_cab_protocol = _registerPort (595,
                                                                         ENetworkProtocol.UDP,
                                                                         "cab-protocol",
                                                                         "CAB Protocol");
  public static final INetworkPort TCP_596_smsd = _registerPort (596, ENetworkProtocol.TCP, "smsd", "SMSD");
  public static final INetworkPort UDP_596_smsd = _registerPort (596, ENetworkProtocol.UDP, "smsd", "SMSD");
  public static final INetworkPort TCP_597_ptcnameservice = _registerPort (597,
                                                                           ENetworkProtocol.TCP,
                                                                           "ptcnameservice",
                                                                           "PTC Name Service");
  public static final INetworkPort UDP_597_ptcnameservice = _registerPort (597,
                                                                           ENetworkProtocol.UDP,
                                                                           "ptcnameservice",
                                                                           "PTC Name Service");
  public static final INetworkPort TCP_598_sco_websrvrmg3 = _registerPort (598,
                                                                           ENetworkProtocol.TCP,
                                                                           "sco-websrvrmg3",
                                                                           "SCO Web Server Manager 3");
  public static final INetworkPort UDP_598_sco_websrvrmg3 = _registerPort (598,
                                                                           ENetworkProtocol.UDP,
                                                                           "sco-websrvrmg3",
                                                                           "SCO Web Server Manager 3");
  public static final INetworkPort TCP_599_acp = _registerPort (599,
                                                                ENetworkProtocol.TCP,
                                                                "acp",
                                                                "Aeolon Core Protocol");
  public static final INetworkPort UDP_599_acp = _registerPort (599,
                                                                ENetworkProtocol.UDP,
                                                                "acp",
                                                                "Aeolon Core Protocol");
  public static final INetworkPort UDP_600_ipcserver = _registerPort (600,
                                                                      ENetworkProtocol.UDP,
                                                                      "ipcserver",
                                                                      "Sun IPC server");
  public static final INetworkPort TCP_600_ipcserver = _registerPort (600,
                                                                      ENetworkProtocol.TCP,
                                                                      "ipcserver",
                                                                      "Sun IPC server");
  public static final INetworkPort UDP_601_syslog_conn = _registerPort (601,
                                                                        ENetworkProtocol.UDP,
                                                                        "syslog-conn",
                                                                        "Reliable Syslog Service");
  public static final INetworkPort TCP_601_syslog_conn = _registerPort (601,
                                                                        ENetworkProtocol.TCP,
                                                                        "syslog-conn",
                                                                        "Reliable Syslog Service");
  public static final INetworkPort UDP_602_xmlrpc_beep = _registerPort (602,
                                                                        ENetworkProtocol.UDP,
                                                                        "xmlrpc-beep",
                                                                        "XML-RPC over BEEP");
  public static final INetworkPort TCP_602_xmlrpc_beep = _registerPort (602,
                                                                        ENetworkProtocol.TCP,
                                                                        "xmlrpc-beep",
                                                                        "XML-RPC over BEEP");
  public static final INetworkPort TCP_603_idxp = _registerPort (603, ENetworkProtocol.TCP, "idxp", "IDXP");
  public static final INetworkPort UDP_603_idxp = _registerPort (603, ENetworkProtocol.UDP, "idxp", "IDXP");
  public static final INetworkPort UDP_604_tunnel = _registerPort (604, ENetworkProtocol.UDP, "tunnel", "TUNNEL");
  public static final INetworkPort TCP_604_tunnel = _registerPort (604, ENetworkProtocol.TCP, "tunnel", "TUNNEL");
  public static final INetworkPort UDP_605_soap_beep = _registerPort (605,
                                                                      ENetworkProtocol.UDP,
                                                                      "soap-beep",
                                                                      "SOAP over BEEP");
  public static final INetworkPort TCP_605_soap_beep = _registerPort (605,
                                                                      ENetworkProtocol.TCP,
                                                                      "soap-beep",
                                                                      "SOAP over BEEP");
  public static final INetworkPort TCP_606_urm = _registerPort (606,
                                                                ENetworkProtocol.TCP,
                                                                "urm",
                                                                "Cray Unified Resource Manager");
  public static final INetworkPort UDP_606_urm = _registerPort (606,
                                                                ENetworkProtocol.UDP,
                                                                "urm",
                                                                "Cray Unified Resource Manager");
  public static final INetworkPort UDP_607_nqs = _registerPort (607, ENetworkProtocol.UDP, "nqs", "nqs");
  public static final INetworkPort TCP_607_nqs = _registerPort (607, ENetworkProtocol.TCP, "nqs", "nqs");
  public static final INetworkPort TCP_608_sift_uft = _registerPort (608,
                                                                     ENetworkProtocol.TCP,
                                                                     "sift-uft",
                                                                     "Sender-Initiated/Unsolicited File Transfer");
  public static final INetworkPort UDP_608_sift_uft = _registerPort (608,
                                                                     ENetworkProtocol.UDP,
                                                                     "sift-uft",
                                                                     "Sender-Initiated/Unsolicited File Transfer");
  public static final INetworkPort TCP_609_npmp_trap = _registerPort (609,
                                                                      ENetworkProtocol.TCP,
                                                                      "npmp-trap",
                                                                      "npmp-trap");
  public static final INetworkPort UDP_609_npmp_trap = _registerPort (609,
                                                                      ENetworkProtocol.UDP,
                                                                      "npmp-trap",
                                                                      "npmp-trap");
  public static final INetworkPort TCP_610_npmp_local = _registerPort (610,
                                                                       ENetworkProtocol.TCP,
                                                                       "npmp-local",
                                                                       "npmp-local");
  public static final INetworkPort UDP_610_npmp_local = _registerPort (610,
                                                                       ENetworkProtocol.UDP,
                                                                       "npmp-local",
                                                                       "npmp-local");
  public static final INetworkPort TCP_611_npmp_gui = _registerPort (611, ENetworkProtocol.TCP, "npmp-gui", "npmp-gui");
  public static final INetworkPort UDP_611_npmp_gui = _registerPort (611, ENetworkProtocol.UDP, "npmp-gui", "npmp-gui");
  public static final INetworkPort UDP_612_hmmp_ind = _registerPort (612,
                                                                     ENetworkProtocol.UDP,
                                                                     "hmmp-ind",
                                                                     "HMMP Indication");
  public static final INetworkPort TCP_612_hmmp_ind = _registerPort (612,
                                                                     ENetworkProtocol.TCP,
                                                                     "hmmp-ind",
                                                                     "HMMP Indication");
  public static final INetworkPort TCP_613_hmmp_op = _registerPort (613,
                                                                    ENetworkProtocol.TCP,
                                                                    "hmmp-op",
                                                                    "HMMP Operation");
  public static final INetworkPort UDP_613_hmmp_op = _registerPort (613,
                                                                    ENetworkProtocol.UDP,
                                                                    "hmmp-op",
                                                                    "HMMP Operation");
  public static final INetworkPort TCP_614_sshell = _registerPort (614, ENetworkProtocol.TCP, "sshell", "SSLshell");
  public static final INetworkPort UDP_614_sshell = _registerPort (614, ENetworkProtocol.UDP, "sshell", "SSLshell");
  public static final INetworkPort UDP_615_sco_inetmgr = _registerPort (615,
                                                                        ENetworkProtocol.UDP,
                                                                        "sco-inetmgr",
                                                                        "Internet Configuration Manager");
  public static final INetworkPort TCP_615_sco_inetmgr = _registerPort (615,
                                                                        ENetworkProtocol.TCP,
                                                                        "sco-inetmgr",
                                                                        "Internet Configuration Manager");
  public static final INetworkPort UDP_616_sco_sysmgr = _registerPort (616,
                                                                       ENetworkProtocol.UDP,
                                                                       "sco-sysmgr",
                                                                       "SCO System Administration Server");
  public static final INetworkPort TCP_616_sco_sysmgr = _registerPort (616,
                                                                       ENetworkProtocol.TCP,
                                                                       "sco-sysmgr",
                                                                       "SCO System Administration Server");
  public static final INetworkPort UDP_617_sco_dtmgr = _registerPort (617,
                                                                      ENetworkProtocol.UDP,
                                                                      "sco-dtmgr",
                                                                      "SCO Desktop Administration Server");
  public static final INetworkPort TCP_617_sco_dtmgr = _registerPort (617,
                                                                      ENetworkProtocol.TCP,
                                                                      "sco-dtmgr",
                                                                      "SCO Desktop Administration Server");
  public static final INetworkPort UDP_618_dei_icda = _registerPort (618, ENetworkProtocol.UDP, "dei-icda", "DEI-ICDA");
  public static final INetworkPort TCP_618_dei_icda = _registerPort (618, ENetworkProtocol.TCP, "dei-icda", "DEI-ICDA");
  public static final INetworkPort TCP_619_compaq_evm = _registerPort (619,
                                                                       ENetworkProtocol.TCP,
                                                                       "compaq-evm",
                                                                       "Compaq EVM");
  public static final INetworkPort UDP_619_compaq_evm = _registerPort (619,
                                                                       ENetworkProtocol.UDP,
                                                                       "compaq-evm",
                                                                       "Compaq EVM");
  public static final INetworkPort UDP_620_sco_websrvrmgr = _registerPort (620,
                                                                           ENetworkProtocol.UDP,
                                                                           "sco-websrvrmgr",
                                                                           "SCO WebServer Manager");
  public static final INetworkPort TCP_620_sco_websrvrmgr = _registerPort (620,
                                                                           ENetworkProtocol.TCP,
                                                                           "sco-websrvrmgr",
                                                                           "SCO WebServer Manager");
  public static final INetworkPort TCP_621_escp_ip = _registerPort (621, ENetworkProtocol.TCP, "escp-ip", "ESCP");
  public static final INetworkPort UDP_621_escp_ip = _registerPort (621, ENetworkProtocol.UDP, "escp-ip", "ESCP");
  public static final INetworkPort TCP_622_collaborator = _registerPort (622,
                                                                         ENetworkProtocol.TCP,
                                                                         "collaborator",
                                                                         "Collaborator");
  public static final INetworkPort UDP_622_collaborator = _registerPort (622,
                                                                         ENetworkProtocol.UDP,
                                                                         "collaborator",
                                                                         "Collaborator");
  public static final INetworkPort TCP_623_oob_ws_http = _registerPort (623,
                                                                        ENetworkProtocol.TCP,
                                                                        "oob-ws-http",
                                                                        "DMTF out-of-band web services management protocol");
  public static final INetworkPort UDP_623_asf_rmcp = _registerPort (623,
                                                                     ENetworkProtocol.UDP,
                                                                     "asf-rmcp",
                                                                     "ASF Remote Management and Control Protocol");
  public static final INetworkPort TCP_624_cryptoadmin = _registerPort (624,
                                                                        ENetworkProtocol.TCP,
                                                                        "cryptoadmin",
                                                                        "Crypto Admin");
  public static final INetworkPort UDP_624_cryptoadmin = _registerPort (624,
                                                                        ENetworkProtocol.UDP,
                                                                        "cryptoadmin",
                                                                        "Crypto Admin");
  public static final INetworkPort UDP_625_dec_dlm = _registerPort (625, ENetworkProtocol.UDP, "dec_dlm", "DEC DLM");
  public static final INetworkPort TCP_625_dec_dlm = _registerPort (625, ENetworkProtocol.TCP, "dec_dlm", "DEC DLM");
  public static final INetworkPort UDP_626_asia = _registerPort (626, ENetworkProtocol.UDP, "asia", "ASIA");
  public static final INetworkPort TCP_626_asia = _registerPort (626, ENetworkProtocol.TCP, "asia", "ASIA");
  public static final INetworkPort TCP_627_passgo_tivoli = _registerPort (627,
                                                                          ENetworkProtocol.TCP,
                                                                          "passgo-tivoli",
                                                                          "PassGo Tivoli");
  public static final INetworkPort UDP_627_passgo_tivoli = _registerPort (627,
                                                                          ENetworkProtocol.UDP,
                                                                          "passgo-tivoli",
                                                                          "PassGo Tivoli");
  public static final INetworkPort TCP_628_qmqp = _registerPort (628, ENetworkProtocol.TCP, "qmqp", "QMQP");
  public static final INetworkPort UDP_628_qmqp = _registerPort (628, ENetworkProtocol.UDP, "qmqp", "QMQP");
  public static final INetworkPort UDP_629_3com_amp3 = _registerPort (629,
                                                                      ENetworkProtocol.UDP,
                                                                      "3com-amp3",
                                                                      "3Com AMP3");
  public static final INetworkPort TCP_629_3com_amp3 = _registerPort (629,
                                                                      ENetworkProtocol.TCP,
                                                                      "3com-amp3",
                                                                      "3Com AMP3");
  public static final INetworkPort TCP_630_rda = _registerPort (630, ENetworkProtocol.TCP, "rda", "RDA");
  public static final INetworkPort UDP_630_rda = _registerPort (630, ENetworkProtocol.UDP, "rda", "RDA");
  public static final INetworkPort UDP_631_ipp = _registerPort (631,
                                                                ENetworkProtocol.UDP,
                                                                "ipp",
                                                                "IPP (Internet Printing Protocol)");
  public static final INetworkPort TCP_631_ipp = _registerPort (631,
                                                                ENetworkProtocol.TCP,
                                                                "ipp",
                                                                "IPP (Internet Printing Protocol)");
  public static final INetworkPort TCP_632_bmpp = _registerPort (632, ENetworkProtocol.TCP, "bmpp", "bmpp");
  public static final INetworkPort UDP_632_bmpp = _registerPort (632, ENetworkProtocol.UDP, "bmpp", "bmpp");
  public static final INetworkPort UDP_633_servstat = _registerPort (633,
                                                                     ENetworkProtocol.UDP,
                                                                     "servstat",
                                                                     "Service Status update (Sterling Software)");
  public static final INetworkPort TCP_633_servstat = _registerPort (633,
                                                                     ENetworkProtocol.TCP,
                                                                     "servstat",
                                                                     "Service Status update (Sterling Software)");
  public static final INetworkPort UDP_634_ginad = _registerPort (634, ENetworkProtocol.UDP, "ginad", "ginad");
  public static final INetworkPort TCP_634_ginad = _registerPort (634, ENetworkProtocol.TCP, "ginad", "ginad");
  public static final INetworkPort TCP_635_rlzdbase = _registerPort (635, ENetworkProtocol.TCP, "rlzdbase", "RLZ DBase");
  public static final INetworkPort UDP_635_rlzdbase = _registerPort (635, ENetworkProtocol.UDP, "rlzdbase", "RLZ DBase");
  public static final INetworkPort TCP_636_ldaps = _registerPort (636,
                                                                  ENetworkProtocol.TCP,
                                                                  "ldaps",
                                                                  "ldap protocol over TLS/SSL (was sldap)");
  public static final INetworkPort UDP_636_ldaps = _registerPort (636,
                                                                  ENetworkProtocol.UDP,
                                                                  "ldaps",
                                                                  "ldap protocol over TLS/SSL (was sldap)");
  public static final INetworkPort UDP_637_lanserver = _registerPort (637,
                                                                      ENetworkProtocol.UDP,
                                                                      "lanserver",
                                                                      "lanserver");
  public static final INetworkPort TCP_637_lanserver = _registerPort (637,
                                                                      ENetworkProtocol.TCP,
                                                                      "lanserver",
                                                                      "lanserver");
  public static final INetworkPort UDP_638_mcns_sec = _registerPort (638, ENetworkProtocol.UDP, "mcns-sec", "mcns-sec");
  public static final INetworkPort TCP_638_mcns_sec = _registerPort (638, ENetworkProtocol.TCP, "mcns-sec", "mcns-sec");
  public static final INetworkPort UDP_639_msdp = _registerPort (639, ENetworkProtocol.UDP, "msdp", "MSDP");
  public static final INetworkPort TCP_639_msdp = _registerPort (639, ENetworkProtocol.TCP, "msdp", "MSDP");
  public static final INetworkPort TCP_640_entrust_sps = _registerPort (640,
                                                                        ENetworkProtocol.TCP,
                                                                        "entrust-sps",
                                                                        "entrust-sps");
  public static final INetworkPort UDP_640_entrust_sps = _registerPort (640,
                                                                        ENetworkProtocol.UDP,
                                                                        "entrust-sps",
                                                                        "entrust-sps");
  public static final INetworkPort UDP_641_repcmd = _registerPort (641, ENetworkProtocol.UDP, "repcmd", "repcmd");
  public static final INetworkPort TCP_641_repcmd = _registerPort (641, ENetworkProtocol.TCP, "repcmd", "repcmd");
  public static final INetworkPort UDP_642_esro_emsdp = _registerPort (642,
                                                                       ENetworkProtocol.UDP,
                                                                       "esro-emsdp",
                                                                       "ESRO-EMSDP V1.3");
  public static final INetworkPort TCP_642_esro_emsdp = _registerPort (642,
                                                                       ENetworkProtocol.TCP,
                                                                       "esro-emsdp",
                                                                       "ESRO-EMSDP V1.3");
  public static final INetworkPort UDP_643_sanity = _registerPort (643, ENetworkProtocol.UDP, "sanity", "SANity");
  public static final INetworkPort TCP_643_sanity = _registerPort (643, ENetworkProtocol.TCP, "sanity", "SANity");
  public static final INetworkPort TCP_644_dwr = _registerPort (644, ENetworkProtocol.TCP, "dwr", "dwr");
  public static final INetworkPort UDP_644_dwr = _registerPort (644, ENetworkProtocol.UDP, "dwr", "dwr");
  public static final INetworkPort UDP_645_pssc = _registerPort (645, ENetworkProtocol.UDP, "pssc", "PSSC");
  public static final INetworkPort TCP_645_pssc = _registerPort (645, ENetworkProtocol.TCP, "pssc", "PSSC");
  public static final INetworkPort TCP_646_ldp = _registerPort (646, ENetworkProtocol.TCP, "ldp", "LDP");
  public static final INetworkPort UDP_646_ldp = _registerPort (646, ENetworkProtocol.UDP, "ldp", "LDP");
  public static final INetworkPort UDP_647_dhcp_failover = _registerPort (647,
                                                                          ENetworkProtocol.UDP,
                                                                          "dhcp-failover",
                                                                          "DHCP Failover");
  public static final INetworkPort TCP_647_dhcp_failover = _registerPort (647,
                                                                          ENetworkProtocol.TCP,
                                                                          "dhcp-failover",
                                                                          "DHCP Failover");
  public static final INetworkPort TCP_648_rrp = _registerPort (648,
                                                                ENetworkProtocol.TCP,
                                                                "rrp",
                                                                "Registry Registrar Protocol (RRP)");
  public static final INetworkPort UDP_648_rrp = _registerPort (648,
                                                                ENetworkProtocol.UDP,
                                                                "rrp",
                                                                "Registry Registrar Protocol (RRP)");
  public static final INetworkPort TCP_649_cadview_3d = _registerPort (649,
                                                                       ENetworkProtocol.TCP,
                                                                       "cadview-3d",
                                                                       "Cadview-3d - streaming 3d models over the internet");
  public static final INetworkPort UDP_649_cadview_3d = _registerPort (649,
                                                                       ENetworkProtocol.UDP,
                                                                       "cadview-3d",
                                                                       "Cadview-3d - streaming 3d models over the internet");
  public static final INetworkPort TCP_650_obex = _registerPort (650, ENetworkProtocol.TCP, "obex", "OBEX");
  public static final INetworkPort UDP_650_obex = _registerPort (650, ENetworkProtocol.UDP, "obex", "OBEX");
  public static final INetworkPort UDP_651_ieee_mms = _registerPort (651, ENetworkProtocol.UDP, "ieee-mms", "IEEE MMS");
  public static final INetworkPort TCP_651_ieee_mms = _registerPort (651, ENetworkProtocol.TCP, "ieee-mms", "IEEE MMS");
  public static final INetworkPort UDP_652_hello_port = _registerPort (652,
                                                                       ENetworkProtocol.UDP,
                                                                       "hello-port",
                                                                       "HELLO_PORT");
  public static final INetworkPort TCP_652_hello_port = _registerPort (652,
                                                                       ENetworkProtocol.TCP,
                                                                       "hello-port",
                                                                       "HELLO_PORT");
  public static final INetworkPort UDP_653_repscmd = _registerPort (653, ENetworkProtocol.UDP, "repscmd", "RepCmd");
  public static final INetworkPort TCP_653_repscmd = _registerPort (653, ENetworkProtocol.TCP, "repscmd", "RepCmd");
  public static final INetworkPort TCP_654_aodv = _registerPort (654, ENetworkProtocol.TCP, "aodv", "AODV");
  public static final INetworkPort UDP_654_aodv = _registerPort (654, ENetworkProtocol.UDP, "aodv", "AODV");
  public static final INetworkPort UDP_655_tinc = _registerPort (655, ENetworkProtocol.UDP, "tinc", "TINC");
  public static final INetworkPort TCP_655_tinc = _registerPort (655, ENetworkProtocol.TCP, "tinc", "TINC");
  public static final INetworkPort TCP_656_spmp = _registerPort (656, ENetworkProtocol.TCP, "spmp", "SPMP");
  public static final INetworkPort UDP_656_spmp = _registerPort (656, ENetworkProtocol.UDP, "spmp", "SPMP");
  public static final INetworkPort UDP_657_rmc = _registerPort (657, ENetworkProtocol.UDP, "rmc", "RMC");
  public static final INetworkPort TCP_657_rmc = _registerPort (657, ENetworkProtocol.TCP, "rmc", "RMC");
  public static final INetworkPort TCP_658_tenfold = _registerPort (658, ENetworkProtocol.TCP, "tenfold", "TenFold");
  public static final INetworkPort UDP_658_tenfold = _registerPort (658, ENetworkProtocol.UDP, "tenfold", "TenFold");
  public static final INetworkPort UDP_660_mac_srvr_admin = _registerPort (660,
                                                                           ENetworkProtocol.UDP,
                                                                           "mac-srvr-admin",
                                                                           "MacOS Server Admin");
  public static final INetworkPort TCP_660_mac_srvr_admin = _registerPort (660,
                                                                           ENetworkProtocol.TCP,
                                                                           "mac-srvr-admin",
                                                                           "MacOS Server Admin");
  public static final INetworkPort TCP_661_hap = _registerPort (661, ENetworkProtocol.TCP, "hap", "HAP");
  public static final INetworkPort UDP_661_hap = _registerPort (661, ENetworkProtocol.UDP, "hap", "HAP");
  public static final INetworkPort UDP_662_pftp = _registerPort (662, ENetworkProtocol.UDP, "pftp", "PFTP");
  public static final INetworkPort TCP_662_pftp = _registerPort (662, ENetworkProtocol.TCP, "pftp", "PFTP");
  public static final INetworkPort TCP_663_purenoise = _registerPort (663,
                                                                      ENetworkProtocol.TCP,
                                                                      "purenoise",
                                                                      "PureNoise");
  public static final INetworkPort UDP_663_purenoise = _registerPort (663,
                                                                      ENetworkProtocol.UDP,
                                                                      "purenoise",
                                                                      "PureNoise");
  public static final INetworkPort UDP_664_asf_secure_rmcp = _registerPort (664,
                                                                            ENetworkProtocol.UDP,
                                                                            "asf-secure-rmcp",
                                                                            "ASF Secure Remote Management and Control Protocol");
  public static final INetworkPort TCP_664_oob_ws_https = _registerPort (664,
                                                                         ENetworkProtocol.TCP,
                                                                         "oob-ws-https",
                                                                         "DMTF out-of-band secure web services management protocol");
  public static final INetworkPort UDP_665_sun_dr = _registerPort (665, ENetworkProtocol.UDP, "sun-dr", "Sun DR");
  public static final INetworkPort TCP_665_sun_dr = _registerPort (665, ENetworkProtocol.TCP, "sun-dr", "Sun DR");
  public static final INetworkPort UDP_666_doom = _registerPort (666, ENetworkProtocol.UDP, "doom", "doom Id Software");
  public static final INetworkPort TCP_666_mdqs = _registerPort (666, ENetworkProtocol.TCP, "mdqs", "");
  public static final INetworkPort UDP_666_mdqs = _registerPort (666, ENetworkProtocol.UDP, "mdqs", "");
  public static final INetworkPort TCP_666_doom = _registerPort (666, ENetworkProtocol.TCP, "doom", "doom Id Software");
  public static final INetworkPort UDP_667_disclose = _registerPort (667,
                                                                     ENetworkProtocol.UDP,
                                                                     "disclose",
                                                                     "campaign contribution disclosures - SDR Technologies");
  public static final INetworkPort TCP_667_disclose = _registerPort (667,
                                                                     ENetworkProtocol.TCP,
                                                                     "disclose",
                                                                     "campaign contribution disclosures - SDR Technologies");
  public static final INetworkPort UDP_668_mecomm = _registerPort (668, ENetworkProtocol.UDP, "mecomm", "MeComm");
  public static final INetworkPort TCP_668_mecomm = _registerPort (668, ENetworkProtocol.TCP, "mecomm", "MeComm");
  public static final INetworkPort TCP_669_meregister = _registerPort (669,
                                                                       ENetworkProtocol.TCP,
                                                                       "meregister",
                                                                       "MeRegister");
  public static final INetworkPort UDP_669_meregister = _registerPort (669,
                                                                       ENetworkProtocol.UDP,
                                                                       "meregister",
                                                                       "MeRegister");
  public static final INetworkPort UDP_670_vacdsm_sws = _registerPort (670,
                                                                       ENetworkProtocol.UDP,
                                                                       "vacdsm-sws",
                                                                       "VACDSM-SWS");
  public static final INetworkPort TCP_670_vacdsm_sws = _registerPort (670,
                                                                       ENetworkProtocol.TCP,
                                                                       "vacdsm-sws",
                                                                       "VACDSM-SWS");
  public static final INetworkPort UDP_671_vacdsm_app = _registerPort (671,
                                                                       ENetworkProtocol.UDP,
                                                                       "vacdsm-app",
                                                                       "VACDSM-APP");
  public static final INetworkPort TCP_671_vacdsm_app = _registerPort (671,
                                                                       ENetworkProtocol.TCP,
                                                                       "vacdsm-app",
                                                                       "VACDSM-APP");
  public static final INetworkPort UDP_672_vpps_qua = _registerPort (672, ENetworkProtocol.UDP, "vpps-qua", "VPPS-QUA");
  public static final INetworkPort TCP_672_vpps_qua = _registerPort (672, ENetworkProtocol.TCP, "vpps-qua", "VPPS-QUA");
  public static final INetworkPort UDP_673_cimplex = _registerPort (673, ENetworkProtocol.UDP, "cimplex", "CIMPLEX");
  public static final INetworkPort TCP_673_cimplex = _registerPort (673, ENetworkProtocol.TCP, "cimplex", "CIMPLEX");
  public static final INetworkPort UDP_674_acap = _registerPort (674, ENetworkProtocol.UDP, "acap", "ACAP");
  public static final INetworkPort TCP_674_acap = _registerPort (674, ENetworkProtocol.TCP, "acap", "ACAP");
  public static final INetworkPort UDP_675_dctp = _registerPort (675, ENetworkProtocol.UDP, "dctp", "DCTP");
  public static final INetworkPort TCP_675_dctp = _registerPort (675, ENetworkProtocol.TCP, "dctp", "DCTP");
  public static final INetworkPort UDP_676_vpps_via = _registerPort (676, ENetworkProtocol.UDP, "vpps-via", "VPPS Via");
  public static final INetworkPort TCP_676_vpps_via = _registerPort (676, ENetworkProtocol.TCP, "vpps-via", "VPPS Via");
  public static final INetworkPort UDP_677_vpp = _registerPort (677,
                                                                ENetworkProtocol.UDP,
                                                                "vpp",
                                                                "Virtual Presence Protocol");
  public static final INetworkPort TCP_677_vpp = _registerPort (677,
                                                                ENetworkProtocol.TCP,
                                                                "vpp",
                                                                "Virtual Presence Protocol");
  public static final INetworkPort TCP_678_ggf_ncp = _registerPort (678,
                                                                    ENetworkProtocol.TCP,
                                                                    "ggf-ncp",
                                                                    "GNU Generation Foundation NCP");
  public static final INetworkPort UDP_678_ggf_ncp = _registerPort (678,
                                                                    ENetworkProtocol.UDP,
                                                                    "ggf-ncp",
                                                                    "GNU Generation Foundation NCP");
  public static final INetworkPort UDP_679_mrm = _registerPort (679, ENetworkProtocol.UDP, "mrm", "MRM");
  public static final INetworkPort TCP_679_mrm = _registerPort (679, ENetworkProtocol.TCP, "mrm", "MRM");
  public static final INetworkPort UDP_680_entrust_aaas = _registerPort (680,
                                                                         ENetworkProtocol.UDP,
                                                                         "entrust-aaas",
                                                                         "entrust-aaas");
  public static final INetworkPort TCP_680_entrust_aaas = _registerPort (680,
                                                                         ENetworkProtocol.TCP,
                                                                         "entrust-aaas",
                                                                         "entrust-aaas");
  public static final INetworkPort TCP_681_entrust_aams = _registerPort (681,
                                                                         ENetworkProtocol.TCP,
                                                                         "entrust-aams",
                                                                         "entrust-aams");
  public static final INetworkPort UDP_681_entrust_aams = _registerPort (681,
                                                                         ENetworkProtocol.UDP,
                                                                         "entrust-aams",
                                                                         "entrust-aams");
  public static final INetworkPort UDP_682_xfr = _registerPort (682, ENetworkProtocol.UDP, "xfr", "XFR");
  public static final INetworkPort TCP_682_xfr = _registerPort (682, ENetworkProtocol.TCP, "xfr", "XFR");
  public static final INetworkPort TCP_683_corba_iiop = _registerPort (683,
                                                                       ENetworkProtocol.TCP,
                                                                       "corba-iiop",
                                                                       "CORBA IIOP");
  public static final INetworkPort UDP_683_corba_iiop = _registerPort (683,
                                                                       ENetworkProtocol.UDP,
                                                                       "corba-iiop",
                                                                       "CORBA IIOP");
  public static final INetworkPort TCP_684_corba_iiop_ssl = _registerPort (684,
                                                                           ENetworkProtocol.TCP,
                                                                           "corba-iiop-ssl",
                                                                           "CORBA IIOP SSL");
  public static final INetworkPort UDP_684_corba_iiop_ssl = _registerPort (684,
                                                                           ENetworkProtocol.UDP,
                                                                           "corba-iiop-ssl",
                                                                           "CORBA IIOP SSL");
  public static final INetworkPort UDP_685_mdc_portmapper = _registerPort (685,
                                                                           ENetworkProtocol.UDP,
                                                                           "mdc-portmapper",
                                                                           "MDC Port Mapper");
  public static final INetworkPort TCP_685_mdc_portmapper = _registerPort (685,
                                                                           ENetworkProtocol.TCP,
                                                                           "mdc-portmapper",
                                                                           "MDC Port Mapper");
  public static final INetworkPort UDP_686_hcp_wismar = _registerPort (686,
                                                                       ENetworkProtocol.UDP,
                                                                       "hcp-wismar",
                                                                       "Hardware Control Protocol Wismar");
  public static final INetworkPort TCP_686_hcp_wismar = _registerPort (686,
                                                                       ENetworkProtocol.TCP,
                                                                       "hcp-wismar",
                                                                       "Hardware Control Protocol Wismar");
  public static final INetworkPort TCP_687_asipregistry = _registerPort (687,
                                                                         ENetworkProtocol.TCP,
                                                                         "asipregistry",
                                                                         "asipregistry");
  public static final INetworkPort UDP_687_asipregistry = _registerPort (687,
                                                                         ENetworkProtocol.UDP,
                                                                         "asipregistry",
                                                                         "asipregistry");
  public static final INetworkPort UDP_688_realm_rusd = _registerPort (688,
                                                                       ENetworkProtocol.UDP,
                                                                       "realm-rusd",
                                                                       "ApplianceWare managment protocol");
  public static final INetworkPort TCP_688_realm_rusd = _registerPort (688,
                                                                       ENetworkProtocol.TCP,
                                                                       "realm-rusd",
                                                                       "ApplianceWare managment protocol");
  public static final INetworkPort TCP_689_nmap = _registerPort (689, ENetworkProtocol.TCP, "nmap", "NMAP");
  public static final INetworkPort UDP_689_nmap = _registerPort (689, ENetworkProtocol.UDP, "nmap", "NMAP");
  public static final INetworkPort UDP_690_vatp = _registerPort (690,
                                                                 ENetworkProtocol.UDP,
                                                                 "vatp",
                                                                 "Velazquez Application Transfer Protocol");
  public static final INetworkPort TCP_690_vatp = _registerPort (690,
                                                                 ENetworkProtocol.TCP,
                                                                 "vatp",
                                                                 "Velazquez Application Transfer Protocol");
  public static final INetworkPort UDP_691_msexch_routing = _registerPort (691,
                                                                           ENetworkProtocol.UDP,
                                                                           "msexch-routing",
                                                                           "MS Exchange Routing");
  public static final INetworkPort TCP_691_msexch_routing = _registerPort (691,
                                                                           ENetworkProtocol.TCP,
                                                                           "msexch-routing",
                                                                           "MS Exchange Routing");
  public static final INetworkPort TCP_692_hyperwave_isp = _registerPort (692,
                                                                          ENetworkProtocol.TCP,
                                                                          "hyperwave-isp",
                                                                          "Hyperwave-ISP");
  public static final INetworkPort UDP_692_hyperwave_isp = _registerPort (692,
                                                                          ENetworkProtocol.UDP,
                                                                          "hyperwave-isp",
                                                                          "Hyperwave-ISP");
  public static final INetworkPort UDP_693_connendp = _registerPort (693,
                                                                     ENetworkProtocol.UDP,
                                                                     "connendp",
                                                                     "almanid Connection Endpoint");
  public static final INetworkPort TCP_693_connendp = _registerPort (693,
                                                                     ENetworkProtocol.TCP,
                                                                     "connendp",
                                                                     "almanid Connection Endpoint");
  public static final INetworkPort TCP_694_ha_cluster = _registerPort (694,
                                                                       ENetworkProtocol.TCP,
                                                                       "ha-cluster",
                                                                       "ha-cluster");
  public static final INetworkPort UDP_694_ha_cluster = _registerPort (694,
                                                                       ENetworkProtocol.UDP,
                                                                       "ha-cluster",
                                                                       "ha-cluster");
  public static final INetworkPort TCP_695_ieee_mms_ssl = _registerPort (695,
                                                                         ENetworkProtocol.TCP,
                                                                         "ieee-mms-ssl",
                                                                         "IEEE-MMS-SSL");
  public static final INetworkPort UDP_695_ieee_mms_ssl = _registerPort (695,
                                                                         ENetworkProtocol.UDP,
                                                                         "ieee-mms-ssl",
                                                                         "IEEE-MMS-SSL");
  public static final INetworkPort UDP_696_rushd = _registerPort (696, ENetworkProtocol.UDP, "rushd", "RUSHD");
  public static final INetworkPort TCP_696_rushd = _registerPort (696, ENetworkProtocol.TCP, "rushd", "RUSHD");
  public static final INetworkPort UDP_697_uuidgen = _registerPort (697, ENetworkProtocol.UDP, "uuidgen", "UUIDGEN");
  public static final INetworkPort TCP_697_uuidgen = _registerPort (697, ENetworkProtocol.TCP, "uuidgen", "UUIDGEN");
  public static final INetworkPort UDP_698_olsr = _registerPort (698, ENetworkProtocol.UDP, "olsr", "OLSR");
  public static final INetworkPort TCP_698_olsr = _registerPort (698, ENetworkProtocol.TCP, "olsr", "OLSR");
  public static final INetworkPort TCP_699_accessnetwork = _registerPort (699,
                                                                          ENetworkProtocol.TCP,
                                                                          "accessnetwork",
                                                                          "Access Network");
  public static final INetworkPort UDP_699_accessnetwork = _registerPort (699,
                                                                          ENetworkProtocol.UDP,
                                                                          "accessnetwork",
                                                                          "Access Network");
  public static final INetworkPort UDP_700_epp = _registerPort (700,
                                                                ENetworkProtocol.UDP,
                                                                "epp",
                                                                "Extensible Provisioning Protocol");
  public static final INetworkPort TCP_700_epp = _registerPort (700,
                                                                ENetworkProtocol.TCP,
                                                                "epp",
                                                                "Extensible Provisioning Protocol");
  public static final INetworkPort UDP_701_lmp = _registerPort (701,
                                                                ENetworkProtocol.UDP,
                                                                "lmp",
                                                                "Link Management Protocol (LMP)");
  public static final INetworkPort TCP_701_lmp = _registerPort (701,
                                                                ENetworkProtocol.TCP,
                                                                "lmp",
                                                                "Link Management Protocol (LMP)");
  public static final INetworkPort UDP_702_iris_beep = _registerPort (702,
                                                                      ENetworkProtocol.UDP,
                                                                      "iris-beep",
                                                                      "IRIS over BEEP");
  public static final INetworkPort TCP_702_iris_beep = _registerPort (702,
                                                                      ENetworkProtocol.TCP,
                                                                      "iris-beep",
                                                                      "IRIS over BEEP");
  public static final INetworkPort UDP_704_elcsd = _registerPort (704,
                                                                  ENetworkProtocol.UDP,
                                                                  "elcsd",
                                                                  "errlog copy/server daemon");
  public static final INetworkPort TCP_704_elcsd = _registerPort (704,
                                                                  ENetworkProtocol.TCP,
                                                                  "elcsd",
                                                                  "errlog copy/server daemon");
  public static final INetworkPort TCP_705_agentx = _registerPort (705, ENetworkProtocol.TCP, "agentx", "AgentX");
  public static final INetworkPort UDP_705_agentx = _registerPort (705, ENetworkProtocol.UDP, "agentx", "AgentX");
  public static final INetworkPort TCP_706_silc = _registerPort (706, ENetworkProtocol.TCP, "silc", "SILC");
  public static final INetworkPort UDP_706_silc = _registerPort (706, ENetworkProtocol.UDP, "silc", "SILC");
  public static final INetworkPort UDP_707_borland_dsj = _registerPort (707,
                                                                        ENetworkProtocol.UDP,
                                                                        "borland-dsj",
                                                                        "Borland DSJ");
  public static final INetworkPort TCP_707_borland_dsj = _registerPort (707,
                                                                        ENetworkProtocol.TCP,
                                                                        "borland-dsj",
                                                                        "Borland DSJ");
  public static final INetworkPort TCP_709_entrust_kmsh = _registerPort (709,
                                                                         ENetworkProtocol.TCP,
                                                                         "entrust-kmsh",
                                                                         "Entrust Key Management Service Handler");
  public static final INetworkPort UDP_709_entrust_kmsh = _registerPort (709,
                                                                         ENetworkProtocol.UDP,
                                                                         "entrust-kmsh",
                                                                         "Entrust Key Management Service Handler");
  public static final INetworkPort TCP_710_entrust_ash = _registerPort (710,
                                                                        ENetworkProtocol.TCP,
                                                                        "entrust-ash",
                                                                        "Entrust Administration Service Handler");
  public static final INetworkPort UDP_710_entrust_ash = _registerPort (710,
                                                                        ENetworkProtocol.UDP,
                                                                        "entrust-ash",
                                                                        "Entrust Administration Service Handler");
  public static final INetworkPort UDP_711_cisco_tdp = _registerPort (711,
                                                                      ENetworkProtocol.UDP,
                                                                      "cisco-tdp",
                                                                      "Cisco TDP");
  public static final INetworkPort TCP_711_cisco_tdp = _registerPort (711,
                                                                      ENetworkProtocol.TCP,
                                                                      "cisco-tdp",
                                                                      "Cisco TDP");
  public static final INetworkPort UDP_712_tbrpf = _registerPort (712, ENetworkProtocol.UDP, "tbrpf", "TBRPF");
  public static final INetworkPort TCP_712_tbrpf = _registerPort (712, ENetworkProtocol.TCP, "tbrpf", "TBRPF");
  public static final INetworkPort UDP_713_iris_xpc = _registerPort (713,
                                                                     ENetworkProtocol.UDP,
                                                                     "iris-xpc",
                                                                     "IRIS over XPC");
  public static final INetworkPort TCP_713_iris_xpc = _registerPort (713,
                                                                     ENetworkProtocol.TCP,
                                                                     "iris-xpc",
                                                                     "IRIS over XPC");
  public static final INetworkPort TCP_714_iris_xpcs = _registerPort (714,
                                                                      ENetworkProtocol.TCP,
                                                                      "iris-xpcs",
                                                                      "IRIS over XPCS");
  public static final INetworkPort UDP_714_iris_xpcs = _registerPort (714,
                                                                      ENetworkProtocol.UDP,
                                                                      "iris-xpcs",
                                                                      "IRIS over XPCS");
  public static final INetworkPort UDP_715_iris_lwz = _registerPort (715, ENetworkProtocol.UDP, "iris-lwz", "IRIS-LWZ");
  public static final INetworkPort TCP_715_iris_lwz = _registerPort (715, ENetworkProtocol.TCP, "iris-lwz", "IRIS-LWZ");
  public static final INetworkPort UDP_716_pana = _registerPort (716, ENetworkProtocol.UDP, "pana", "PANA Messages");
  public static final INetworkPort UDP_729_netviewdm1 = _registerPort (729,
                                                                       ENetworkProtocol.UDP,
                                                                       "netviewdm1",
                                                                       "IBM NetView DM/6000 Server/Client");
  public static final INetworkPort TCP_729_netviewdm1 = _registerPort (729,
                                                                       ENetworkProtocol.TCP,
                                                                       "netviewdm1",
                                                                       "IBM NetView DM/6000 Server/Client");
  public static final INetworkPort UDP_730_netviewdm2 = _registerPort (730,
                                                                       ENetworkProtocol.UDP,
                                                                       "netviewdm2",
                                                                       "IBM NetView DM/6000 send/tcp");
  public static final INetworkPort TCP_730_netviewdm2 = _registerPort (730,
                                                                       ENetworkProtocol.TCP,
                                                                       "netviewdm2",
                                                                       "IBM NetView DM/6000 send/tcp");
  public static final INetworkPort UDP_731_netviewdm3 = _registerPort (731,
                                                                       ENetworkProtocol.UDP,
                                                                       "netviewdm3",
                                                                       "IBM NetView DM/6000 receive/tcp");
  public static final INetworkPort TCP_731_netviewdm3 = _registerPort (731,
                                                                       ENetworkProtocol.TCP,
                                                                       "netviewdm3",
                                                                       "IBM NetView DM/6000 receive/tcp");
  public static final INetworkPort TCP_741_netgw = _registerPort (741, ENetworkProtocol.TCP, "netgw", "netGW");
  public static final INetworkPort UDP_741_netgw = _registerPort (741, ENetworkProtocol.UDP, "netgw", "netGW");
  public static final INetworkPort UDP_742_netrcs = _registerPort (742,
                                                                   ENetworkProtocol.UDP,
                                                                   "netrcs",
                                                                   "Network based Rev. Cont. Sys.");
  public static final INetworkPort TCP_742_netrcs = _registerPort (742,
                                                                   ENetworkProtocol.TCP,
                                                                   "netrcs",
                                                                   "Network based Rev. Cont. Sys.");
  public static final INetworkPort TCP_744_flexlm = _registerPort (744,
                                                                   ENetworkProtocol.TCP,
                                                                   "flexlm",
                                                                   "Flexible License Manager");
  public static final INetworkPort UDP_744_flexlm = _registerPort (744,
                                                                   ENetworkProtocol.UDP,
                                                                   "flexlm",
                                                                   "Flexible License Manager");
  public static final INetworkPort UDP_747_fujitsu_dev = _registerPort (747,
                                                                        ENetworkProtocol.UDP,
                                                                        "fujitsu-dev",
                                                                        "Fujitsu Device Control");
  public static final INetworkPort TCP_747_fujitsu_dev = _registerPort (747,
                                                                        ENetworkProtocol.TCP,
                                                                        "fujitsu-dev",
                                                                        "Fujitsu Device Control");
  public static final INetworkPort TCP_748_ris_cm = _registerPort (748,
                                                                   ENetworkProtocol.TCP,
                                                                   "ris-cm",
                                                                   "Russell Info Sci Calendar Manager");
  public static final INetworkPort UDP_748_ris_cm = _registerPort (748,
                                                                   ENetworkProtocol.UDP,
                                                                   "ris-cm",
                                                                   "Russell Info Sci Calendar Manager");
  public static final INetworkPort UDP_749_kerberos_adm = _registerPort (749,
                                                                         ENetworkProtocol.UDP,
                                                                         "kerberos-adm",
                                                                         "kerberos administration");
  public static final INetworkPort TCP_749_kerberos_adm = _registerPort (749,
                                                                         ENetworkProtocol.TCP,
                                                                         "kerberos-adm",
                                                                         "kerberos administration");
  public static final INetworkPort UDP_750_loadav = _registerPort (750, ENetworkProtocol.UDP, "loadav", "");
  public static final INetworkPort TCP_750_rfile = _registerPort (750, ENetworkProtocol.TCP, "rfile", "");
  public static final INetworkPort UDP_750_kerberos_iv = _registerPort (750,
                                                                        ENetworkProtocol.UDP,
                                                                        "kerberos-iv",
                                                                        "kerberos version iv");
  public static final INetworkPort TCP_751_pump = _registerPort (751, ENetworkProtocol.TCP, "pump", "");
  public static final INetworkPort UDP_751_pump = _registerPort (751, ENetworkProtocol.UDP, "pump", "");
  public static final INetworkPort UDP_752_qrh = _registerPort (752, ENetworkProtocol.UDP, "qrh", "");
  public static final INetworkPort TCP_752_qrh = _registerPort (752, ENetworkProtocol.TCP, "qrh", "");
  public static final INetworkPort UDP_753_rrh = _registerPort (753, ENetworkProtocol.UDP, "rrh", "");
  public static final INetworkPort TCP_753_rrh = _registerPort (753, ENetworkProtocol.TCP, "rrh", "");
  public static final INetworkPort TCP_754_tell = _registerPort (754, ENetworkProtocol.TCP, "tell", "send");
  public static final INetworkPort UDP_754_tell = _registerPort (754, ENetworkProtocol.UDP, "tell", "send");
  public static final INetworkPort TCP_758_nlogin = _registerPort (758, ENetworkProtocol.TCP, "nlogin", "");
  public static final INetworkPort UDP_758_nlogin = _registerPort (758, ENetworkProtocol.UDP, "nlogin", "");
  public static final INetworkPort TCP_759_con = _registerPort (759, ENetworkProtocol.TCP, "con", "");
  public static final INetworkPort UDP_759_con = _registerPort (759, ENetworkProtocol.UDP, "con", "");
  public static final INetworkPort TCP_760_ns = _registerPort (760, ENetworkProtocol.TCP, "ns", "");
  public static final INetworkPort UDP_760_ns = _registerPort (760, ENetworkProtocol.UDP, "ns", "");
  public static final INetworkPort UDP_761_rxe = _registerPort (761, ENetworkProtocol.UDP, "rxe", "");
  public static final INetworkPort TCP_761_rxe = _registerPort (761, ENetworkProtocol.TCP, "rxe", "");
  public static final INetworkPort TCP_762_quotad = _registerPort (762, ENetworkProtocol.TCP, "quotad", "");
  public static final INetworkPort UDP_762_quotad = _registerPort (762, ENetworkProtocol.UDP, "quotad", "");
  public static final INetworkPort TCP_763_cycleserv = _registerPort (763, ENetworkProtocol.TCP, "cycleserv", "");
  public static final INetworkPort UDP_763_cycleserv = _registerPort (763, ENetworkProtocol.UDP, "cycleserv", "");
  public static final INetworkPort TCP_764_omserv = _registerPort (764, ENetworkProtocol.TCP, "omserv", "");
  public static final INetworkPort UDP_764_omserv = _registerPort (764, ENetworkProtocol.UDP, "omserv", "");
  public static final INetworkPort UDP_765_webster = _registerPort (765, ENetworkProtocol.UDP, "webster", "");
  public static final INetworkPort TCP_765_webster = _registerPort (765, ENetworkProtocol.TCP, "webster", "");
  public static final INetworkPort UDP_767_phonebook = _registerPort (767, ENetworkProtocol.UDP, "phonebook", "phone");
  public static final INetworkPort TCP_767_phonebook = _registerPort (767, ENetworkProtocol.TCP, "phonebook", "phone");
  public static final INetworkPort UDP_769_vid = _registerPort (769, ENetworkProtocol.UDP, "vid", "");
  public static final INetworkPort TCP_769_vid = _registerPort (769, ENetworkProtocol.TCP, "vid", "");
  public static final INetworkPort TCP_770_cadlock = _registerPort (770, ENetworkProtocol.TCP, "cadlock", "");
  public static final INetworkPort UDP_770_cadlock = _registerPort (770, ENetworkProtocol.UDP, "cadlock", "");
  public static final INetworkPort UDP_771_rtip = _registerPort (771, ENetworkProtocol.UDP, "rtip", "");
  public static final INetworkPort TCP_771_rtip = _registerPort (771, ENetworkProtocol.TCP, "rtip", "");
  public static final INetworkPort UDP_772_cycleserv2 = _registerPort (772, ENetworkProtocol.UDP, "cycleserv2", "");
  public static final INetworkPort TCP_772_cycleserv2 = _registerPort (772, ENetworkProtocol.TCP, "cycleserv2", "");
  public static final INetworkPort UDP_773_notify = _registerPort (773, ENetworkProtocol.UDP, "notify", "");
  public static final INetworkPort TCP_773_submit = _registerPort (773, ENetworkProtocol.TCP, "submit", "");
  public static final INetworkPort TCP_774_rpasswd = _registerPort (774, ENetworkProtocol.TCP, "rpasswd", "");
  public static final INetworkPort UDP_774_acmaint_dbd = _registerPort (774, ENetworkProtocol.UDP, "acmaint_dbd", "");
  public static final INetworkPort TCP_775_entomb = _registerPort (775, ENetworkProtocol.TCP, "entomb", "");
  public static final INetworkPort UDP_775_acmaint_transd = _registerPort (775,
                                                                           ENetworkProtocol.UDP,
                                                                           "acmaint_transd",
                                                                           "");
  public static final INetworkPort TCP_776_wpages = _registerPort (776, ENetworkProtocol.TCP, "wpages", "");
  public static final INetworkPort UDP_776_wpages = _registerPort (776, ENetworkProtocol.UDP, "wpages", "");
  public static final INetworkPort UDP_777_multiling_http = _registerPort (777,
                                                                           ENetworkProtocol.UDP,
                                                                           "multiling-http",
                                                                           "Multiling HTTP");
  public static final INetworkPort TCP_777_multiling_http = _registerPort (777,
                                                                           ENetworkProtocol.TCP,
                                                                           "multiling-http",
                                                                           "Multiling HTTP");
  public static final INetworkPort TCP_780_wpgs = _registerPort (780, ENetworkProtocol.TCP, "wpgs", "");
  public static final INetworkPort UDP_780_wpgs = _registerPort (780, ENetworkProtocol.UDP, "wpgs", "");
  public static final INetworkPort TCP_800_mdbs_daemon = _registerPort (800, ENetworkProtocol.TCP, "mdbs_daemon", "");
  public static final INetworkPort UDP_800_mdbs_daemon = _registerPort (800, ENetworkProtocol.UDP, "mdbs_daemon", "");
  public static final INetworkPort UDP_801_device = _registerPort (801, ENetworkProtocol.UDP, "device", "");
  public static final INetworkPort TCP_801_device = _registerPort (801, ENetworkProtocol.TCP, "device", "");
  public static final INetworkPort TCP_810_fcp_udp = _registerPort (810, ENetworkProtocol.TCP, "fcp-udp", "FCP");
  public static final INetworkPort UDP_810_fcp_udp = _registerPort (810,
                                                                    ENetworkProtocol.UDP,
                                                                    "fcp-udp",
                                                                    "FCP Datagram");
  public static final INetworkPort UDP_828_itm_mcell_s = _registerPort (828,
                                                                        ENetworkProtocol.UDP,
                                                                        "itm-mcell-s",
                                                                        "itm-mcell-s");
  public static final INetworkPort TCP_828_itm_mcell_s = _registerPort (828,
                                                                        ENetworkProtocol.TCP,
                                                                        "itm-mcell-s",
                                                                        "itm-mcell-s");
  public static final INetworkPort UDP_829_pkix_3_ca_ra = _registerPort (829,
                                                                         ENetworkProtocol.UDP,
                                                                         "pkix-3-ca-ra",
                                                                         "PKIX-3 CA/RA");
  public static final INetworkPort TCP_829_pkix_3_ca_ra = _registerPort (829,
                                                                         ENetworkProtocol.TCP,
                                                                         "pkix-3-ca-ra",
                                                                         "PKIX-3 CA/RA");
  public static final INetworkPort UDP_830_netconf_ssh = _registerPort (830,
                                                                        ENetworkProtocol.UDP,
                                                                        "netconf-ssh",
                                                                        "NETCONF over SSH");
  public static final INetworkPort TCP_830_netconf_ssh = _registerPort (830,
                                                                        ENetworkProtocol.TCP,
                                                                        "netconf-ssh",
                                                                        "NETCONF over SSH");
  public static final INetworkPort TCP_831_netconf_beep = _registerPort (831,
                                                                         ENetworkProtocol.TCP,
                                                                         "netconf-beep",
                                                                         "NETCONF over BEEP");
  public static final INetworkPort UDP_831_netconf_beep = _registerPort (831,
                                                                         ENetworkProtocol.UDP,
                                                                         "netconf-beep",
                                                                         "NETCONF over BEEP");
  public static final INetworkPort UDP_832_netconfsoaphttp = _registerPort (832,
                                                                            ENetworkProtocol.UDP,
                                                                            "netconfsoaphttp",
                                                                            "NETCONF for SOAP over HTTPS");
  public static final INetworkPort TCP_832_netconfsoaphttp = _registerPort (832,
                                                                            ENetworkProtocol.TCP,
                                                                            "netconfsoaphttp",
                                                                            "NETCONF for SOAP over HTTPS");
  public static final INetworkPort UDP_833_netconfsoapbeep = _registerPort (833,
                                                                            ENetworkProtocol.UDP,
                                                                            "netconfsoapbeep",
                                                                            "NETCONF for SOAP over BEEP");
  public static final INetworkPort TCP_833_netconfsoapbeep = _registerPort (833,
                                                                            ENetworkProtocol.TCP,
                                                                            "netconfsoapbeep",
                                                                            "NETCONF for SOAP over BEEP");
  public static final INetworkPort TCP_847_dhcp_failover2 = _registerPort (847,
                                                                           ENetworkProtocol.TCP,
                                                                           "dhcp-failover2",
                                                                           "dhcp-failover 2");
  public static final INetworkPort UDP_847_dhcp_failover2 = _registerPort (847,
                                                                           ENetworkProtocol.UDP,
                                                                           "dhcp-failover2",
                                                                           "dhcp-failover 2");
  public static final INetworkPort TCP_848_gdoi = _registerPort (848, ENetworkProtocol.TCP, "gdoi", "GDOI");
  public static final INetworkPort UDP_848_gdoi = _registerPort (848, ENetworkProtocol.UDP, "gdoi", "GDOI");
  public static final INetworkPort UDP_860_iscsi = _registerPort (860, ENetworkProtocol.UDP, "iscsi", "iSCSI");
  public static final INetworkPort TCP_860_iscsi = _registerPort (860, ENetworkProtocol.TCP, "iscsi", "iSCSI");
  public static final INetworkPort UDP_861_owamp_control = _registerPort (861,
                                                                          ENetworkProtocol.UDP,
                                                                          "owamp-control",
                                                                          "OWAMP-Control");
  public static final INetworkPort TCP_861_owamp_control = _registerPort (861,
                                                                          ENetworkProtocol.TCP,
                                                                          "owamp-control",
                                                                          "OWAMP-Control");
  public static final INetworkPort TCP_862_twamp_control = _registerPort (862,
                                                                          ENetworkProtocol.TCP,
                                                                          "twamp-control",
                                                                          "Two-way Active Measurement Protocol (TWAMP) Control");
  public static final INetworkPort UDP_862_twamp_control = _registerPort (862,
                                                                          ENetworkProtocol.UDP,
                                                                          "twamp-control",
                                                                          "Two-way Active Measurement Protocol (TWAMP) Control");
  public static final INetworkPort TCP_873_rsync = _registerPort (873, ENetworkProtocol.TCP, "rsync", "rsync");
  public static final INetworkPort UDP_873_rsync = _registerPort (873, ENetworkProtocol.UDP, "rsync", "rsync");
  public static final INetworkPort UDP_886_iclcnet_locate = _registerPort (886,
                                                                           ENetworkProtocol.UDP,
                                                                           "iclcnet-locate",
                                                                           "ICL coNETion locate server");
  public static final INetworkPort TCP_886_iclcnet_locate = _registerPort (886,
                                                                           ENetworkProtocol.TCP,
                                                                           "iclcnet-locate",
                                                                           "ICL coNETion locate server");
  public static final INetworkPort TCP_887_iclcnet_svinfo = _registerPort (887,
                                                                           ENetworkProtocol.TCP,
                                                                           "iclcnet_svinfo",
                                                                           "ICL coNETion server info");
  public static final INetworkPort UDP_887_iclcnet_svinfo = _registerPort (887,
                                                                           ENetworkProtocol.UDP,
                                                                           "iclcnet_svinfo",
                                                                           "ICL coNETion server info");
  public static final INetworkPort TCP_888_cddbp = _registerPort (888,
                                                                  ENetworkProtocol.TCP,
                                                                  "cddbp",
                                                                  "CD Database Protocol");
  public static final INetworkPort UDP_888_accessbuilder = _registerPort (888,
                                                                          ENetworkProtocol.UDP,
                                                                          "accessbuilder",
                                                                          "AccessBuilder");
  public static final INetworkPort TCP_888_accessbuilder = _registerPort (888,
                                                                          ENetworkProtocol.TCP,
                                                                          "accessbuilder",
                                                                          "AccessBuilder");
  public static final INetworkPort TCP_900_omginitialrefs = _registerPort (900,
                                                                           ENetworkProtocol.TCP,
                                                                           "omginitialrefs",
                                                                           "OMG Initial Refs");
  public static final INetworkPort UDP_900_omginitialrefs = _registerPort (900,
                                                                           ENetworkProtocol.UDP,
                                                                           "omginitialrefs",
                                                                           "OMG Initial Refs");
  public static final INetworkPort UDP_901_smpnameres = _registerPort (901,
                                                                       ENetworkProtocol.UDP,
                                                                       "smpnameres",
                                                                       "SMPNAMERES");
  public static final INetworkPort TCP_901_smpnameres = _registerPort (901,
                                                                       ENetworkProtocol.TCP,
                                                                       "smpnameres",
                                                                       "SMPNAMERES");
  public static final INetworkPort TCP_902_ideafarm_door = _registerPort (902,
                                                                          ENetworkProtocol.TCP,
                                                                          "ideafarm-door",
                                                                          "self documenting Telnet Door");
  public static final INetworkPort UDP_902_ideafarm_door = _registerPort (902,
                                                                          ENetworkProtocol.UDP,
                                                                          "ideafarm-door",
                                                                          "self documenting Door: send 0x00 for info");
  public static final INetworkPort TCP_903_ideafarm_panic = _registerPort (903,
                                                                           ENetworkProtocol.TCP,
                                                                           "ideafarm-panic",
                                                                           "self documenting Telnet Panic Door");
  public static final INetworkPort UDP_903_ideafarm_panic = _registerPort (903,
                                                                           ENetworkProtocol.UDP,
                                                                           "ideafarm-panic",
                                                                           "self documenting Panic Door: send 0x00 for info");
  public static final INetworkPort UDP_910_kink = _registerPort (910,
                                                                 ENetworkProtocol.UDP,
                                                                 "kink",
                                                                 "Kerberized Internet Negotiation of Keys (KINK)");
  public static final INetworkPort TCP_910_kink = _registerPort (910,
                                                                 ENetworkProtocol.TCP,
                                                                 "kink",
                                                                 "Kerberized Internet Negotiation of Keys (KINK)");
  public static final INetworkPort TCP_911_xact_backup = _registerPort (911,
                                                                        ENetworkProtocol.TCP,
                                                                        "xact-backup",
                                                                        "xact-backup");
  public static final INetworkPort UDP_911_xact_backup = _registerPort (911,
                                                                        ENetworkProtocol.UDP,
                                                                        "xact-backup",
                                                                        "xact-backup");
  public static final INetworkPort TCP_912_apex_mesh = _registerPort (912,
                                                                      ENetworkProtocol.TCP,
                                                                      "apex-mesh",
                                                                      "APEX relay-relay service");
  public static final INetworkPort UDP_912_apex_mesh = _registerPort (912,
                                                                      ENetworkProtocol.UDP,
                                                                      "apex-mesh",
                                                                      "APEX relay-relay service");
  public static final INetworkPort UDP_913_apex_edge = _registerPort (913,
                                                                      ENetworkProtocol.UDP,
                                                                      "apex-edge",
                                                                      "APEX endpoint-relay service");
  public static final INetworkPort TCP_913_apex_edge = _registerPort (913,
                                                                      ENetworkProtocol.TCP,
                                                                      "apex-edge",
                                                                      "APEX endpoint-relay service");
  public static final INetworkPort TCP_989_ftps_data = _registerPort (989,
                                                                      ENetworkProtocol.TCP,
                                                                      "ftps-data",
                                                                      "ftp protocol, data, over TLS/SSL");
  public static final INetworkPort UDP_989_ftps_data = _registerPort (989,
                                                                      ENetworkProtocol.UDP,
                                                                      "ftps-data",
                                                                      "ftp protocol, data, over TLS/SSL");
  public static final INetworkPort TCP_990_ftps = _registerPort (990,
                                                                 ENetworkProtocol.TCP,
                                                                 "ftps",
                                                                 "ftp protocol, control, over TLS/SSL");
  public static final INetworkPort UDP_990_ftps = _registerPort (990,
                                                                 ENetworkProtocol.UDP,
                                                                 "ftps",
                                                                 "ftp protocol, control, over TLS/SSL");
  public static final INetworkPort UDP_991_nas = _registerPort (991,
                                                                ENetworkProtocol.UDP,
                                                                "nas",
                                                                "Netnews Administration System");
  public static final INetworkPort TCP_991_nas = _registerPort (991,
                                                                ENetworkProtocol.TCP,
                                                                "nas",
                                                                "Netnews Administration System");
  public static final INetworkPort UDP_992_telnets = _registerPort (992,
                                                                    ENetworkProtocol.UDP,
                                                                    "telnets",
                                                                    "telnet protocol over TLS/SSL");
  public static final INetworkPort TCP_992_telnets = _registerPort (992,
                                                                    ENetworkProtocol.TCP,
                                                                    "telnets",
                                                                    "telnet protocol over TLS/SSL");
  public static final INetworkPort UDP_993_imaps = _registerPort (993,
                                                                  ENetworkProtocol.UDP,
                                                                  "imaps",
                                                                  "imap4 protocol over TLS/SSL");
  public static final INetworkPort TCP_993_imaps = _registerPort (993,
                                                                  ENetworkProtocol.TCP,
                                                                  "imaps",
                                                                  "imap4 protocol over TLS/SSL");
  public static final INetworkPort TCP_994_ircs = _registerPort (994,
                                                                 ENetworkProtocol.TCP,
                                                                 "ircs",
                                                                 "irc protocol over TLS/SSL");
  public static final INetworkPort UDP_994_ircs = _registerPort (994,
                                                                 ENetworkProtocol.UDP,
                                                                 "ircs",
                                                                 "irc protocol over TLS/SSL");
  public static final INetworkPort TCP_995_pop3s = _registerPort (995,
                                                                  ENetworkProtocol.TCP,
                                                                  "pop3s",
                                                                  "pop3 protocol over TLS/SSL (was spop3)");
  public static final INetworkPort UDP_995_pop3s = _registerPort (995,
                                                                  ENetworkProtocol.UDP,
                                                                  "pop3s",
                                                                  "pop3 protocol over TLS/SSL (was spop3)");
  public static final INetworkPort UDP_996_vsinet = _registerPort (996, ENetworkProtocol.UDP, "vsinet", "vsinet");
  public static final INetworkPort TCP_996_vsinet = _registerPort (996, ENetworkProtocol.TCP, "vsinet", "vsinet");
  public static final INetworkPort TCP_997_maitrd = _registerPort (997, ENetworkProtocol.TCP, "maitrd", "");
  public static final INetworkPort UDP_997_maitrd = _registerPort (997, ENetworkProtocol.UDP, "maitrd", "");
  public static final INetworkPort TCP_998_busboy = _registerPort (998, ENetworkProtocol.TCP, "busboy", "");
  public static final INetworkPort UDP_998_puparp = _registerPort (998, ENetworkProtocol.UDP, "puparp", "");
  public static final INetworkPort TCP_999_garcon = _registerPort (999, ENetworkProtocol.TCP, "garcon", "");
  public static final INetworkPort TCP_999_puprouter = _registerPort (999, ENetworkProtocol.TCP, "puprouter", "");
  public static final INetworkPort UDP_999_puprouter = _registerPort (999, ENetworkProtocol.UDP, "puprouter", "");
  public static final INetworkPort UDP_999_applix = _registerPort (999, ENetworkProtocol.UDP, "applix", "Applix ac");
  public static final INetworkPort TCP_1000_cadlock2 = _registerPort (1000, ENetworkProtocol.TCP, "cadlock2", "");
  public static final INetworkPort UDP_1000_cadlock2 = _registerPort (1000, ENetworkProtocol.UDP, "cadlock2", "");
  public static final INetworkPort TCP_1010_surf = _registerPort (1010, ENetworkProtocol.TCP, "surf", "surf");
  public static final INetworkPort UDP_1010_surf = _registerPort (1010, ENetworkProtocol.UDP, "surf", "surf");
  public static final INetworkPort UDP_1021_exp1 = _registerPort (1021,
                                                                  ENetworkProtocol.UDP,
                                                                  "exp1",
                                                                  "RFC3692-style Experiment 1 (*) [RFC4727]");
  public static final INetworkPort TCP_1021_exp1 = _registerPort (1021,
                                                                  ENetworkProtocol.TCP,
                                                                  "exp1",
                                                                  "RFC3692-style Experiment 1 (*) [RFC4727]");
  public static final INetworkPort UDP_1022_exp2 = _registerPort (1022,
                                                                  ENetworkProtocol.UDP,
                                                                  "exp2",
                                                                  "RFC3692-style Experiment 2 (*) [RFC4727]");
  public static final INetworkPort TCP_1022_exp2 = _registerPort (1022,
                                                                  ENetworkProtocol.TCP,
                                                                  "exp2",
                                                                  "RFC3692-style Experiment 2 (*) [RFC4727]");
  public static final INetworkPort UDP_1023_ = _registerPort (1023, ENetworkProtocol.UDP, "", "Reserved");
  public static final INetworkPort TCP_1023_ = _registerPort (1023, ENetworkProtocol.TCP, "", "Reserved");

  @Nonnull
  private static INetworkPort _registerPort (@Nonnegative final int nPort,
                                             @Nonnull final ENetworkProtocol eProtocol,
                                             @Nonnull final String sName,
                                             @Nonnull final String sDescription)
  {
    final NetworkPort aPort = new NetworkPort (nPort, eProtocol, sName, sDescription);
    s_aPortList.add (aPort);
    return aPort;
  }

  @SuppressWarnings ("unused")
  @PresentForCodeCoverage
  private static final DefaultNetworkPorts s_aInstance = new DefaultNetworkPorts ();

  private DefaultNetworkPorts ()
  {}

  /**
   * Check of the passed port number is theoretically valid. Valid ports must be
   * in the range of 0-65535.
   * 
   * @param nPort
   *        The port number to be tested.
   * @return <code>true</code> if the port number is valid, <code>false</code>
   *         otherwise.
   */
  public static boolean isValidPort (final int nPort)
  {
    return nPort >= CNetworkPort.MINIMUM_PORT_NUMBER && nPort <= CNetworkPort.MAXIMUM_PORT_NUMBER;
  }

  /**
   * @return A non-null list with all known ports.
   */
  @Nonnull
  @ReturnsMutableCopy
  public static List <NetworkPort> getAllPorts ()
  {
    return ContainerHelper.newList (s_aPortList);
  }

  @Nonnull
  @ReturnsMutableCopy
  public static List <NetworkPort> getAllPorts (@Nonnegative final int nPort)
  {
    final List <NetworkPort> ret = new ArrayList <NetworkPort> ();
    for (final NetworkPort aPort : s_aPortList)
      if (aPort.getPort () == nPort)
        ret.add (aPort);
    return ret;
  }
}
