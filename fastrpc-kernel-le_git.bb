DESCRIPTION = "Fastrpc Kernel driver"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://include/linux/fastrpc.h;beginline=1;endline=4;md5=acb731fae05ccd6f44205970f6e3abed"

SUMMARY = "adsprpc-kernel libraries"

inherit autotools linux-kernel-base deploy

PR = "r0"

DEPENDS = "rsync-native"
DEPENDS += "bc-native bison-native"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

FILESPATH   =+ "${WORKSPACE}:"
SRC_URI     =  "file://vendor/qcom/opensource/dsp-kernel/"
SRC_URI     += "file://start_dsp_le"
SRC_URI     += "file://dsp.service"

S = "${WORKDIR}/vendor/qcom/opensource/dsp-kernel"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"

# Disable parallel make
PARALLEL_MAKE = "-j1"

KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"

do_compile[lockfiles] = "${TMPDIR}/build_modules.lock"

do_compile() {
  cd ${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform  &&
  BUILD_CONFIG=msm-kernel/build.config.msm.${MACHINE}.le \
  EXT_MODULES=../../vendor/qcom/opensource/dsp-kernel \
  ROOTDIR=${WORKSPACE}/ \
  MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/dsp-kernel \
  OUT_DIR=temp_out_dir \
  KERNEL_KIT=${KERNEL_OUT_PATH}/ \
  KERNEL_UAPI_HEADERS_DIR=${STAGING_KERNEL_BUILDDIR} \
  INPLACE_COMPILE=y \
  ./build/build_module.sh
}

do_install() {
  install -d ${D}${sysconfdir}/initscripts
  install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
  install -d ${D}/${includedir}/linux

  install -m 0755 ${S}/include/linux/fastrpc.h ${D}/${includedir}/linux
  install -m 755 ${WORKDIR}/start_dsp_le ${D}${sysconfdir}/initscripts
  sed -i 's/frpc-trusted-adsprpc/frpc-adsprpc/g' ${D}${sysconfdir}/initscripts/start_dsp_le
  install -m 0755 ${S}/frpc-adsprpc.ko -D ${WORKDIR}/${base_libdir}/modules/${KERNEL_VERSION}/frpc-adsprpc.ko

  install -m 0755 ${S}/frpc-adsprpc.ko -D ${D}${libdir}/modules/frpc-adsprpc.ko
  install -m 0644 ${WORKDIR}/dsp.service -D ${D}${systemd_unitdir}/system/dsp.service
  ln -sf ${systemd_unitdir}/system/dsp.service ${D}${systemd_unitdir}/system/multi-user.target.wants/dsp.service
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
FILES:${PN}-dev += "/include"
FILES:${PN}-dev += "/include/linux"
