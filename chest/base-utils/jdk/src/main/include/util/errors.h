/* !!! commented out conflicts ...#ifndef ACE_OS_H */
#ifndef _UTL_ERRORS_H_
#define _UTL_ERRORS_H_
/*
 * File:
 *    errors.h
 * Contents:
 *
 * 	Error codes for EXC_TYPE return codes.
 *
 *		NOTE: The values are set so as not to clash with "errno.h"
 *
 * Created by:
 *    Lyor Goldstein.
 *
 * History:
 *    28-Mar-1995 (Lyor G.) -  derived from Intel's iRMX "rmxerr.h"
 *    07-nov-1995 (Alon G.) -  added EBAD
 *
 * Remarks:
 *
 *    EOK - which signals no-error - defined in "_types.h"
 */

/*---------------------------------------------------------------------------*/

#include <errno.h>

/* * * * * * * * * * * * * * * * * * * * * * * */
/*                                             */
/*   E N V I R O N M E N T A L    E R R O R S  */
/*                                             */
/* * * * * * * * * * * * * * * * * * * * * * * */

#ifndef EEXIST
#	define EEXIST						0x2001
#endif

#define EMEM                  0x2002    /* memory allocation exceeded */
#ifndef EOVERFLOW
#	define EOVERFLOW             0x2003    /* some value is too large */
#endif
#define ELIMIT                0x2004    /* some limit exceeded */
#define ECONTEXT              0x2005    /* request out of context */
#define ESTATE                0x2007    /* ready task resumed */
#define ENOTCONFIGURED        0x2008
#define EINTERRUPTSATURATION  0x2009    
                            /* interrupt task accumulated the maximum allowable
                             * number of SIGNAL$INTERRUPT requests */
#define EINTERRUPTOVERFLOW   0x200a
                            /* interrupt task accumulated more than the max
                             * allowable number of SIGNAL$INTERRUPT requests */
#define ETRANSMISSION         0x200b    /* error in message transmission */
#define ESLOT                 0x200c    /* there are no available GDT slots */
#define EDATACHAIN            0x200d    /* buffer returned is a data chain */

/* 
 * IOS exception codes 
 */
#define EFEXIST            0x2020    /* file already exists */
#define EFNEXIST           0x2021    /* non-existant File  */
#define EDEVFD             0x2022    /* device and file driver incompatible */
#define ESUPPORT           0x2023    /* function not supported */
#define EEMPTYENTRY        0x2024    /* directory entry is empty */
#define EDIREND            0x2025    /* no more directory entries */
#define EFACCESS           0x2026    /* access to file denied */
#define EFTYPE             0x2027    /* invalid file type */
#define ESHARE             0x2028    /* file cannot be shared with others */
#define ESPACE             0x2029    /* insufficient space on volume */
#define EIDDR              0x202a    /* bad device driver request */
#define EFLUSHING          0x202c    /* other end of stream file is gone */
#define EILLVOL            0x202d    /* illegal volume type */
#define EDEVOFFLINE        0x202e    /* device is off line */
#define EIFDR              0x202f    /* illegal file driver request */
#define EFRAGMENTATION     0x2030 /* file too fragmented to extend */
#define EDIRNOTEMPTY       0x2031    /* directory not empty */
#define ENOTFILECONN       0x2032    /* not a file connection */
#define ENOTDEVICECONN     0x2033 /* not a device connection */
#define ECONNNOTOPEN       0x2034    /* connection is not open for requested operation*/
#define ECONNOPEN          0x2035    /* connection already open */
#define EBUFFEREDCONN      0x2036    /* connection opened by EIOS but now accessed by 
                                      * BIOS */
#define EOUTSTANDINGCONNS  0x2037  /* specified soft detach has left device 
                                    * connections intact */
#define EALREADYATTACHED  0x2038    /* device already attached */
#define EDEVDETACHING      0x2039    /* specified file on a device
                                      * in the process of being detached */
#define ENOTSAMEDEVICE     0x203a    /* existing and new pathnames not on same
                                      * device */
#define EILLOGICALRENAME   0x203b    /* new pathname includes existing pathname */
#define ESTREAMSPECIAL     0x203c    /* stream file request out of context */
#define EINVALIDFNODE      0x203d    /* invalid file descriptor */
#define EPATHNAMESYNTAX    0x203e    /* pathname null or contains
                                      * invalid characters */
#define EFNODELIMIT        0x203f    /* insufficient fnodes on volume */

/*
 * EIOS exception codes 
 */
#define ELOGNAMESYNTAX     0x2040    /* invalid logical name */
#define ECANNOTCLOSE       0x2041    /* buffers cannot be flushed */
#define EIOMEM             0x2042    /* IO system has insufficent memory */
#define EMEDIA             0x2044    /* no disk in drive */
#define ELOGNAMENEXIST     0x2045    /* logical name does not exist  */
#define ENOTOWNER          0x2046    /* user trying to detach a device is not
                                      * the device's owner */
#define EIOJOB             0x2047    /* job is not a valid IO job */
#define EUDFFORMAT         0x2048    /* udf is corrupted */
#define ENAMENEXIST        0x2049    /* user name not present in udf */
#define EUIDNEXIST         0x204a    /* user token doesn't match udf */
#define EPASSWORDMISMATCH  0x204b    /* incorrect password */
#define EUDFIO             0x204c    /* specified UDF file cannot be found */

/* 
 * Expanded IO exception codes 
 */
#define EIOUNCLASS         0x2050    /* an unclassified error has occured */
#define EIOSOFT            0x2051    /* soft error has occured */
#define EIOHARD            0x2052    /* hard error has occured */
#define EIOOPRINT          0x2053    /* device is not ready */
#define EIOWRPROT          0x2054    /* write protected */
#define EIONODATA          0x2055    /* no data on the next TAPE record */
#define EIOMODE            0x2056    /* a tape drive attempted a read/write operation
                                      * before the previous one completed */
#define EIONOSPARES        0x2057    /* an attempt was made to assign an alternate
                                      * track, but no more were available */
#define EIOALTASSIGNED     0x2058    /* an alternate was assigned during this I/O 
                                      * operation */

/* 
 * Application Loader exception codes 
 */
#define EBADHEADER         0x2062    /* an invalid object file header */
#define EEOF               0x2065    /* unexpected End of File while reading a rec */
#define ENOLOADERMEM       0x2067
#define ENOSTART           0x206c    /* the Application Loader could not find the
                                      * Start address */
#define EJOBSIZE           0x206d    /* The max. memory pool size of job being loaded
                                      * is smaller than the amount required to load
                                      * it */
#define EOVERLAY           0x206e    /* the overlay name does not match */
#define ELOADERSUPPORT     0x206f    /* The file requires features not supported by 
                                      * those configuration of the app-loader */

/* 
 * Human Interface exception codes 
 */
#define ELITERAL           0x2080    /* literal with no closing quote detected 
                                      * while parsing */
#define ESTRINGBUFFER      0x2081    /* buffer too small for O.S. returned string */
#define ESEPARATOR         0x2082    /* illegal command separator */
#define ECONTINUED         0x2083    /* user parse buffer is continued */
#define EINVALIDNUMERIC    0x2084    /* invalid form of number */
#define ELIST              0x2085    /* missing value-list value */
#define EWILDCARD          0x2086    /* invalid wildcard character usage */
#define EPREPOSITION       0x2087    /* invalid preposition usage */
#define EPATH              0x2088    /* invalid path name */
#define ECONTROLC          0x2089    /* job cancelled via a control-C */
#define ECONTROL           0x208a    /* invalid control */
#define EUNMATCHEDLISTS    0x208b
#define EINVALIDDATE       0x208c
#define ENOPARAMETERS      0x208d    /* no parameters found in command line */
#define EVERSION           0x208e    /* version of entered command
                                      * incompatible with system */
#define EGETPATHORDER      0x208f    /* get$output$pathname called
                                      * before get$input$pathname */
#define EPERMISSION        0x2090    /* do not have proper access */
#define EINVALIDTIME       0x2091    /* setting of time was invalid */

 /* 
  * UDI exception codes 
  */
#define EUNKNOWNEXIT       0x20c0    /* normal termination */
#define EWARNINGEXIT       0x20c1    /* warning termination */
#define EERROREXIT         0x20c2    /* error termination */
#define EFATALEXIT         0x20c3    /* fatal error termination */
#define EABORTEXIT         0x20c4    /* user program aborted */
#define EUDIINTERNAL       0x20c5    /* unrecoverable internal error */

/* 
 * Nucleus Communication Service exception codes 
 */
#ifndef ECANCELLED
#define ECANCELLED 0x20e1    /* RSVP transaction cancelled by remote host */
#endif

#define EHOSTID            0x20e2    /* invalid host id parameter */
#define ENOLOCALBUFFER     0x20e3    /* insufficient buffer available on local host 
                                      * to receive message */
#define ENOREMOTEBUFFER    0x20e4    /* insufficient buffer available on remote 
                                      * host to receive message */
#define ERESOURCELIMIT     0x20e6    /* exceeded limit of number of simultaneous 
                                      * messages */
#define ETRANSID           0x20e8    /* invalid transaction id parameter */
#define EDISCONNECTED      0x20e9    /* null socket parameter used with port that is
                                      * not connected */
#define ETRANSLIMIT        0x20ea    /* exceeded limit of number of simultaneous 
                                      * transactions */

/* User defined errors range */

#define EUSERFIRSTERR   0x6000
#define EUSERLASTERR    0x7FFF

/* * * * * * * * * * * * * * * * * * * * */
/*                                       */
/*  P R O G R A M M I N G   E R R O R S  */
/*                                       */
/* * * * * * * * * * * * * * * * * * * * */

/* 
 * Nucleus exception codes 
 */
#define EZERODIVIDE        0x8000
#define ETYPE              0x8002    /* token parameter is of invalid type */
#define EPARAM             0x8004    /* parameter has an invalid value */
#define EBADCALL           0x8005    /* An OS extension received an invalid code */
#define EARRAYBOUNDS       0x8006    /* array overflow*/
#define ENDPERROR          0x8007    /* NPX error has occured */
#define EILLEGALOPCODE     0x8008
#define EEMULATORTRAP      0x8009    /* an ESC instruction was encountered with 
                                      * emulator bit set in MSW */
#define ECHECKEXCEPTION    0x800a    /* a PASCAL task exceeded CASE statement 
                                      * boundary */
#define ECPUXFERDATALIMIT  0x800b /* the NPX tried to access an address that 
                                   * is out of segment bound */
#define EPROTECTION        0x800d    /* General Protection error */
#define ENOTPRESENT        0x800e    /* A request to load a segment register 
                                   * whose segment is not present */
#define EBADADDR           0x800f    /* Invalid logical address */

/* 
 * IOS exception codes
 * EIOS exception codes 
 */
#define ENOUSER            0x8021    /* no default user is defined */
#define ENOPREFIX          0x8022    /* no default prefix is defined */
#define EBADBUFF           0x8023    /* specified buffer too small for requested
                                      * operation */
#define ENOTLOGNAME        0x8040    /* the specified object is not a device or 
                                      * file connection */
#define ENOTDEVICE         0x8041    /* the specified object is not a device 
                                      * connection */
#define ENOTCONNECTION     0x8042    /* the specified object is not a file 
                                      * connection */

/* 
 * Application Loader exception codes 
 */
#define EJOBPARAM          0x8060    /* maximum memory specified is less than the
                                      * minimum memory specified */

/* 
 * HI exception codes 
 */
#define EPARSETABLES       0x8080    /* internal error in parse tables */
#define EJOBTABLES         0x8081    /* internal inconsistency in job tables */
#define EDEFAULTSO         0x8083    /* default output name string specified is 
                                      * invalid */
#define ESTRING            0x8084    /* Returned pathname exceeds 255 characters
                                      * in length */
#define EERROROUTPUT       0x8085    /* send$eo$response called when command 
                                      * connection allows only send$co$response */

/* 
 * UDI exception codes 
 */
#define ERESERVEPARAM      0x80c6    /* calling program attempted to reserve 
                                      * more than 12 files or buffers */
#define EOPENPARAM         0x80c7    /* calling program attempted to open a file
                                      * with more than two buffers */

/* 
 * Nucleus Communication Service exception codes 
 */
#define EPROTOCOL          0x80e0    /* port parameter is wrong protocol */
#define EPORTIDUSED        0x80e1    /* request port id is in use */
#define ENUCBADBUF         0x80e2    /* invalid buffer pointer or insufficient 
                                      * buffer length */
/*---------------------------------------------------------------------------*/

/* 
 * specific error codes.
 */
#define EBAD               0x8100    /* general bad value */

#ifndef ENOMSG
#	define ENOMSG             0x8101
#endif
#ifndef EIDRM
#	define EIDRM              0x8102
#endif
#define ECHRNG             0x8103
#define EL2NSYNC           0x8104
#define EL3HLT             0x8105
#define EL3RST             0x8106
#define ELNRNG             0x8107
#define EUNATCH            0x8108
#define ENOCSI             0x8109
#define EL2HLT             0x810a

#ifndef ECANCELED
#define ECANCELED          0x810b
#endif

#define EBADE              0x810c
#define EBADR              0x810d
#define EXFULL             0x810e
#define ENOANO             0x810f
#define EBADRQC            0x8110
#define EBADSLT            0x8111

#ifndef EDEADLOCK
#define EDEADLOCK          0x8112
#endif

#define EBFONT             0x8113

#ifndef ENOSTR
#	define ENOSTR             0x8114
#endif

#ifndef ENODATA
#	define ENODATA            0x8115
#endif

#ifndef ETIME
#	define ETIME              0x8116
#endif

#ifndef ENOSR
#	define ENOSR              0x8117
#endif

#define ENONET             0x8118
#define ENOPKG             0x8119
#define EREMOTE            0x811a
#ifndef ENOLINK
#	define ENOLINK            0x811b
#endif
#define EADV               0x811c
#define ESRMNT             0x811d
#define ECOMM              0x811e

#ifndef EPROTO
#	define EPROTO             0x811f
#endif

#define EMULTIHOP          0x8120

#ifndef EBADMSG
#	define EBADMSG            0x8121
#endif

#define ENOTUNIQ           0x8122
#define EBADFD             0x8123
#define EREMCHG            0x8124
#define ELIBACC            0x8125
#define ELIBBAD            0x8126
#define ELIBSCN            0x8127
#define ELIBMAX            0x8128
#define ELIBEXEC           0x8129

#ifndef EILSEQ
#define EILSEQ             0x812a
#endif

#ifndef ENOSYS
#define ENOSYS             0x812b
#endif

#define ERESTART           0x812c
#define ESTRPIPE           0x812d
#define EUSERS             0x812e
#define ESTALE             0x812f

/*---------------------------------------------------------------------------*/

#endif /* of ifdef _errors_h_ */
/* !!! commented out conflicts ...#endif ACE_OS_H */
