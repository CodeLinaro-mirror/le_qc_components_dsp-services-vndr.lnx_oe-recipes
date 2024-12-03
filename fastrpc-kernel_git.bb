DESCRIPTION = "Fastrpc Kernel drivers"
LICENSE = "GPL-2.0"
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

SIGN_PATH = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/scripts', d)}"
CERT_PATH = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/certs', d)}"
KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"

do_compile[lockfiles] = "${TMPDIR}/build_modules.lock"

#do_configure() {
#  cp -f ${WORKSPACE}/vendor/qcom/opensource/dsp-kernel/Makefile.am ${WORKSPACE}/vendor/qcom/opensource/dsp-kernel/Makefile
#  cp -f ${WORKSPACE}/vendor/qcom/opensource/dsp-kernel/Kbuild.am ${WORKSPACE}/vendor/qcom/opensource/dsp-kernel/Kbuild
#}

do_compile() {
  cd ${KERNEL_PLATFORM_PATH}
  BUILD_CONFIG=msm-kernel/${KERNEL_CONFIG} \
  EXT_MODULES=${EXT_MODULES} \
  ROOTDIR=${WORKSPACE}/ \
  MODULE_OUT=${S} \
  OUT_DIR=${WORKDIR}/out/${KERNEL_DEFCONFIG} \
  KERNEL_KIT=${KERNEL_OUT_PATH}/ \
  KERNEL_UAPI_HEADERS_DIR=${STAGING_KERNEL_BUILDDIR} \
  INPLACE_COMPILE=y \
  ./build/build_module.sh
}

do_install() {
  install -d ${D}${sysconfdir}/initscripts
  install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
  install -d ${D}/${includedir}/linux
  install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}

  install -m 0755 ${S}/include/linux/fastrpc.h ${D}/${includedir}/linux
  install -m 755 ${WORKDIR}/start_dsp_le ${D}${sysconfdir}/initscripts
  install -m 0755 ${S}/frpc-adsprpc.ko -D ${WORKDIR}/${base_libdir}/modules/${KERNEL_VERSION}/frpc-adsprpc.ko
  install -m 0755 ${S}/cdsp-loader.ko -D ${WORKDIR}/${base_libdir}/modules/${KERNEL_VERSION}/cdsp-loader.ko

  # strip debug symbols and sign the module
  ${STRIP} --strip-debug ${S}/frpc-adsprpc.ko

  #LD_LIBRARY_PATH=${KERNEL_PLATFORM_PATH}/prebuilts/kernel-build-tools/linux-x86/lib64/ \
  #${KERNEL_PLATFORM_PATH}/${SIGN_PATH}/sign-file sha1 ${CERT_PATH}/signing_key.pem \
  #${CERT_PATH}/signing_key.x509 ${S}/frpc-adsprpc.ko

  install -m 0755 ${S}/frpc-adsprpc.ko -D ${D}${libdir}/modules/frpc-adsprpc.ko
  install -m 0755 ${S}/cdsp-loader.ko -D ${D}${libdir}/modules/cdsp-loader.ko
  install -m 0644 ${WORKDIR}/dsp.service -D ${D}${systemd_unitdir}/system/dsp.service
  ln -sf ${systemd_unitdir}/system/dsp.service ${D}${systemd_unitdir}/system/multi-user.target.wants/dsp.service

  install -m 0644 ${S}/Module.symvers -D ${D}${base_libdir}/modules/${KERNEL_VERSION}/fastrpc-kernel/Module.symvers
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
FILES:${PN} += "${base_libdir}/modules/${KERNEL_VERSION}/fastrpc-kernel/Module.symvers"
FILES:${PN}-dev += "/include"
FILES:${PN}-dev += "/include/linux"
