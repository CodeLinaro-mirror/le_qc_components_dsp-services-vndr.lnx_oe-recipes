DESCRIPTION = "Fastrpc Kernel drivers"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://include/linux/fastrpc.h;beginline=1;endline=4;md5=acb731fae05ccd6f44205970f6e3abed"

SUMMARY = "adsprpc-kernel libraries"

inherit module deploy
PROVIDES = "kernel-module-fastrpc-kernel"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

FILESPATH   =+ "${WORKSPACE}:"
DSP_KERNEL_DIR = "${WORKDIR}/vendor/qcom/opensource/dsp-kernel"

SRC_URI = "file://vendor/qcom/opensource/dsp-kernel/ \
file://start_dsp_le \
file://dsp.service"

S = "${DSP_KERNEL_DIR}"

PARALLEL_MAKE = "-j 1"

EXTRA_OEMAKE += "M=${S}"
DEPENDS += "virtual/kernel-toolchain-native"

KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"
MAKE_TARGETS = "modules"

do_install() {
  install -d ${D}${sysconfdir}/initscripts \
  ${D}${systemd_unitdir}/system/multi-user.target.wants/ \
  ${D}${includedir}/linux

  install -m 0755 ${S}/include/linux/fastrpc.h ${D}/${includedir}/linux
  install -m 755 ${WORKDIR}/start_dsp_le ${D}${sysconfdir}/initscripts

  for ko in ${S}/*.ko; do
    # Install to WORKDIR
    install -m 0755 $ko -D ${WORKDIR}/${base_libdir}/modules/${KERNEL_VERSION}/$(basename $ko)
    # Strip debug symbols
    ${STRIP} --strip-debug $ko
    # Install to final destination
    install -m 0755 $ko -D ${D}${libdir}/modules/$(basename $ko)
  done

  install -m 0644 ${WORKDIR}/dsp.service -D ${D}${systemd_unitdir}/system/dsp.service
  ln -sf ${systemd_unitdir}/system/dsp.service ${D}${systemd_unitdir}/system/multi-user.target.wants/dsp.service

  install -m 0644 ${S}/Module.symvers -D ${D}${includedir}/kernel-module-fastrpc-kernel/Module.symvers
}

do_deploy() {
  install -d ${DEPLOYDIR}/kernel_modules
  for kmod in $(find ${D} -name "*.ko") ; do
    install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
  done
}

addtask do_deploy after do_install

FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "/etc/initscripts/start_dsp_le"
FILES:${PN} += "${libdir}/modules/*"
FILES:${PN} += "${systemd_unitdir}/system/dsp.service"
FILES:${PN} += "${systemd_unitdir}/system/multi-user.target.wants/dsp.service"
FILES:${PN} += "${includedir}/kernel-module-fastrpc-kernel/Module.symvers"
FILES:${PN}-dev += "/include"
FILES:${PN}-dev += "/include/linux"
