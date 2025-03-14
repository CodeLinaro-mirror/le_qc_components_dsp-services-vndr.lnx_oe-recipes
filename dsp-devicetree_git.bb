DESCRIPTION       = "Fastrpc devicetree"
LICENSE           = "GPL-2.0"
LIC_FILES_CHKSUM = "file://Makefile;md5=febcda0bf58d4266ffa5a125675c3fc3"

inherit linux-kernel-base deploy

FILESPATH   =+ "${WORKSPACE}:"
SRC_URI     =  "file://vendor/qcom/opensource/dsp-devicetree"

S = "${WORKDIR}/vendor/qcom/opensource/dsp-devicetree"

do_configure[depends] = "virtual/kernel:do_shared_workdir"

KERNEL_VERSION = "${@get_kernelversion_headers('${STAGING_KERNEL_BUILDDIR}')}"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"

# Disable parallel make
PARALLEL_MAKE = ""

# Disable parallel make
PARALLEL_MAKE = "-j1"

do_compile[lockfiles] = "${TMPDIR}/build_modules.lock"

do_configure () {
	:
}

EXT_MODULES = "${@os.path.relpath("${S}", "${KERNEL_PLATFORM_PATH}")}"
INTERMEDIAT_KERNEL_PATH = "${WORKDIR}/out/${KERNEL_DEFCONFIG}"

do_compile() {
    cd ${KERNEL_PLATFORM_PATH}
    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=${EXT_MODULES} \
    ROOTDIR=${WORKSPACE}/ \
    MODULE_OUT=${S} \
    KERNEL_KIT=${KERNEL_OUT_PATH}/ \
    OUT_DIR=${INTERMEDIAT_KERNEL_PATH}  \
    INPLACE_COMPILE=y \
    ./build/build_module.sh
}

do_deploy() {
    install -d ${DEPLOYDIR}/tech_dtbs
    install -m 0644 ${S}/${MACHINE}/*.dtbo ${DEPLOYDIR}/tech_dtbs
}

addtask do_deploy after do_install

FILES:${PN} += "${sysconfdir}/*"
ALLOW_EMPTY:${PN} = "1"
